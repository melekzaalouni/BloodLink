# BloodLink : parties du rapport du Membre 2 (gRPC et Docker)

> Brouillon à relire et à reformuler avec tes propres mots avant de l'intégrer au rapport commun.
> Les passages marqués **[À COMPLÉTER]** demandent une mesure ou une capture que toi seul(e) peux faire.

## 3. Justification du protocole gRPC (stock-service)

`stock-service` est un service **interne** : il n'est appelé que par `donor-service`, jamais par un
navigateur. Cela change les priorités par rapport à l'API REST publique.

| Critère | gRPC (choisi) | REST (écarté) |
|---|---|---|
| Format | Binaire (Protocol Buffers), compact | JSON texte, plus volumineux |
| Transport | HTTP/2, connexion réutilisée | HTTP/1.1 le plus souvent |
| Contrat | `.proto` obligatoire, classes Java générées côté serveur et client | Optionnel (OpenAPI écrit à part) |
| Streaming | Natif (server streaming) | Absent, il faut du polling ou SSE |
| Erreurs | Statuts gRPC typés (`NOT_FOUND`, `INVALID_ARGUMENT`...) | Codes HTTP |

**Apport du streaming.** `StreamStockLevels` envoie plusieurs réponses pour une seule requête :
le client reçoit les niveaux de stock à intervalle régulier sans les redemander. Avec REST, le
client devrait interroger le serveur en boucle.

**Alternative rejetée : REST.** Il aurait fonctionné, mais sans contrat typé partagé ni streaming
natif, pour un appel interne où la lisibilité par un navigateur n'apporte rien.

## 4. Contrat gRPC : `stock.proto`

Le contrat est écrit **avant** le code (approche *contract-first*) et publié dans
`contracts/stock.proto`, puis copié dans `stock-service/src/main/proto/`. Le Membre 4 génère son
client à partir du même fichier.

- Package `bloodlink.stock.v1`, option `java_package = tn.bloodlink.stock.grpc`.
- Service `StockService` avec trois méthodes : `GetStock` (unaire), `UpdateStock` (unaire, delta
  positif ou négatif) et `StreamStockLevels` (server streaming).
- Messages : `GetStockRequest`, `UpdateStockRequest`, `StreamStockRequest`, `StockLevel`
  (type, unités, seuil, urgent, date en secondes epoch).

**Ce qui est généré.** Le plugin `protobuf-maven-plugin` lance `protoc` pendant `mvn compile`.
Il produit dans `target/generated-sources/protobuf/` les classes de messages (`java/`) et le
squelette `StockServiceGrpc` avec `StockServiceImplBase` (`grpc-java/`). Le serveur étend
`StockServiceImplBase` ; le client utilise un stub.

**Règles d'évolution du contrat.**

1. Le numéro après le signe `=` identifie le champ sur le réseau : on ne le change jamais et on ne
   le réutilise jamais.
2. Pour ajouter une donnée, on ajoute un champ avec un **nouveau numéro** : les anciens clients
   l'ignorent, rien ne casse.
3. Pour retirer un champ, on le marque `reserved` (numéro et nom) afin que personne ne le réutilise.
4. Un changement incompatible crée un nouveau package `bloodlink.stock.v2` à côté de `v1`.

**Statuts d'erreur.** Les exceptions métier ne connaissent pas gRPC. La classe
`GrpcExceptionAdvice` (`@GrpcAdvice`) les convertit :

| Situation | Exception | Statut gRPC |
|---|---|---|
| Groupe sanguin invalide (règle R1) | `InvalidBloodTypeException` | `INVALID_ARGUMENT` |
| Aucune ligne de stock | `StockNotFoundException` | `NOT_FOUND` |
| Retrait supérieur au stock | `InsufficientStockException` | `FAILED_PRECONDITION` |

La règle R4 (stock urgent) est portée par l'entité : `urgent = units < threshold`.

## 6. Docker et choix cloud-native

**Dockerfile multi-stage.** Le premier stage (`maven:3.9-eclipse-temurin-17`) compile le projet ;
le second (`eclipse-temurin:17-jre-alpine`) ne contient que le JRE et le jar. L'image finale n'a
pas d'outils de build, elle est plus petite et expose moins de surface d'attaque. `pom.xml` est
copié avant `src` pour que Docker réutilise la couche des dépendances quand seul le code change.
Le processus tourne avec un utilisateur non root (`USER app`). Le build utilise `-DskipTests` :
l'image ne doit pas dépendre d'une base de données ; les tests se lancent avec `mvn test`.

| Image | Taille |
|---|---|
| Multi-stage (`bloodlink/stock`) | **[À COMPLÉTER]** : `docker images bloodlink/stock` |
| Un seul stage (`docs/Dockerfile.single-stage`) | **[À COMPLÉTER]** : `docker images bloodlink/stock-single` |

**Docker Compose.** Un seul fichier décrit tout le système : trois bases PostgreSQL (une par
service), le réseau `bloodlink-net`, des volumes nommés et les services. Chaque service est joint
par son nom (DNS interne de Docker) : `stock-db`, `stock-service`... jamais `localhost` dans un
conteneur.

- **Healthchecks** : `pg_isready` pour les bases, `/actuator/health` (via `wget`) pour les
  services, avec `start_period` pour laisser Spring Boot démarrer.
- **Ordre de démarrage** : `depends_on` avec `condition: service_healthy`. Le `depends_on` simple
  attend seulement que le conteneur démarre ; PostgreSQL met quelques secondes avant d'accepter
  des connexions. `service_healthy` attend que le healthcheck soit vert.
- **Volumes nommés** : les données survivent à `docker compose down` ; `down -v` les efface.
- **Ports publiés** : seulement ce qui sert à la démo (9090 pour `grpcurl`).

**Choix cloud-native.**

| Exigence | Réalisation |
|---|---|
| Configuration externalisée | Variables `${VAR:défaut}` dans `application.yml`, valeurs dans `.env` |
| Aucun secret dans le dépôt | `.env` ignoré par Git, `.env.example` avec de fausses valeurs, pas de défaut pour `DB_PASSWORD` |
| Services sans état | L'état est uniquement dans PostgreSQL : on redémarre un conteneur, rien n'est perdu |
| Logs sur la sortie standard | Aucun fichier de logs, `docker compose logs stock-service` |
| Endpoints de santé | Actuator `/actuator/health` |
| Découverte par DNS | Noms de services Compose dans les URLs |

**Test « machine propre » : [À COMPLÉTER]** date, machine, résultat de `docker compose ps`.

## 9. Difficultés, limites et améliorations (partie gRPC et Docker)

- Le streaming interroge la base en boucle et occupe un thread serveur par client. Amélioration :
  pousser les changements depuis une source d'événements (broker de messages).
- `UpdateStock` ne verrouille pas la ligne : deux retraits simultanés peuvent se chevaucher.
  Amélioration : verrou optimiste (`@Version`) ou pessimiste.
- Les contrats sont copiés à la main entre `contracts/` et `stock-service/src/main/proto/`.
  Amélioration : une seule source de vérité.
- `ddl-auto: update` au lieu de migrations versionnées (Flyway).
- La communication gRPC est en clair (plaintext) à l'intérieur du réseau Docker, sans TLS.
- **[À COMPLÉTER]** difficultés réellement rencontrées (verrouillage de `target` sous Windows,
  contrat différent entre ton premier essai et celui de l'équipe, etc.).
