# BloodLink

BloodLink est une plateforme de gestion du don de sang construite en microservices (Spring Boot 3, Java 17).
Elle met en relation trois acteurs : les **donneurs**, qui s'inscrivent et réservent un rendez-vous de don ;
les **hôpitaux**, qui expriment des besoins urgents en sang ; et les **banques de sang**, qui gèrent leur stock.
Chaque service expose le protocole le mieux adapté à son rôle : REST pour les clients web et mobile,
gRPC pour le stock, SOAP pour l'échange avec les hôpitaux. Tout est lancé par une seule commande Docker Compose.

## Architecture

![Schéma d'architecture](docs/architecture.png)

| Service | Protocole | Port(s) | Base de données | Rôle |
|---|---|---|---|---|
| api-gateway | HTTP | 8080 | - | Point d'entrée unique, redirige vers les services |
| donor-service | REST / JSON | 8081 | donor_db (PostgreSQL) | Donneurs, rendez-vous, demandes de sang |
| stock-service | gRPC | 9090 (actuator 8082) | stock_db (PostgreSQL) | Stock de sang |
| hospital-service | SOAP | 8083 (`/ws`) | hospital_db (PostgreSQL) | Échanges avec les hôpitaux |

Règle d'architecture : **une base de données par service**. Aucune clé étrangère ne traverse les bases
(par exemple `external_ref` dans donor-service est un simple nombre, l'identifiant donné par l'hôpital).

## Prérequis

Docker et Docker Compose. Rien d'autre à installer pour lancer le projet.

## Lancer le projet

```bash
git clone https://github.com/melekzaalouni/BloodLink.git
cd BloodLink
cp .env.example .env
docker compose up --build
```

Le premier démarrage télécharge les images et compile les services : comptez quelques minutes.
Pour savoir que tout est prêt :

```bash
docker compose ps        # tous les services doivent être "running" / "healthy"
```

Pour arrêter : `docker compose down` (ajoutez `-v` pour supprimer aussi les données des bases).

## URLs utiles

| Quoi | URL |
|---|---|
| Gateway | http://localhost:8080 |
| Swagger UI (donor-service) | http://localhost:8081/swagger-ui.html |
| Contrat OpenAPI (JSON) | http://localhost:8081/v3/api-docs |
| Santé donor-service | http://localhost:8081/actuator/health |
| Santé via la gateway | http://localhost:8080/actuator/health |
| gRPC (stock-service) | localhost:9090 |
| WSDL SOAP (hospital-service) | http://localhost:8083/ws/hospital.wsdl |

## donor-service (REST)

### Routes

| Méthode | URI | Succès | Erreurs |
|---|---|---|---|
| POST | `/api/v1/donors` | 201 + `Location` | 400, 409 |
| GET | `/api/v1/donors` | 200 (page de 20 par défaut) | - |
| GET | `/api/v1/donors/{id}` | 200 | 404 |
| PUT | `/api/v1/donors/{id}` | 200 | 400, 404, 409 |
| DELETE | `/api/v1/donors/{id}` | 204 | 404 |
| POST | `/api/v1/donors/{id}/appointments` | 201 + `Location` | 400, 404, 422 |
| GET | `/api/v1/donors/{id}/appointments` | 200 | 404 |

Les URI utilisent des noms au pluriel et jamais de verbes : la ressource est dans l'URL, l'action est le verbe HTTP.
Le préfixe `/api/v1` versionne l'API.

### Règles métier

| Règle | Description | Résultat si violée |
|---|---|---|
| R1 | Groupe sanguin valide : A, B, AB ou O suivi de + ou - | 400 |
| R2 | Adresse e-mail unique par donneur | 409 |
| R3 | Au moins 56 jours entre deux dons (un premier don est toujours autorisé) | 422 |

### Format d'erreur

Toutes les erreurs ont le même format JSON, produit par un seul gestionnaire global ; aucune trace de pile n'est renvoyée :

```json
{
  "timestamp": "2026-11-27T21:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/donors",
  "details": ["bloodType : bloodType must be A, B, AB or O followed by + or -"]
}
```

### Exemples d'appels

```bash
# Créer un donneur -> 201 + en-tête Location
curl -i -X POST http://localhost:8081/api/v1/donors -H 'Content-Type: application/json' \
  -d '{"fullName":"Awa Diallo","email":"awa@mail.com","phone":"22790123456","bloodType":"O+","lastDonation":"2026-11-10"}'

# Groupe sanguin invalide -> 400 avec details
curl -i -X POST http://localhost:8081/api/v1/donors -H 'Content-Type: application/json' \
  -d '{"fullName":"X","email":"x@mail.com","bloodType":"Z+"}'

# Donneur inconnu -> 404
curl -i http://localhost:8081/api/v1/donors/9999

# Rendez-vous trop proche du dernier don (R3) -> 422
curl -i -X POST http://localhost:8081/api/v1/donors/1/appointments -H 'Content-Type: application/json' \
  -d '{"bankName":"CNTS Niamey","scheduledAt":"2026-12-01T10:00:00"}'

# Rendez-vous après 56 jours -> 201
curl -i -X POST http://localhost:8081/api/v1/donors/1/appointments -H 'Content-Type: application/json' \
  -d '{"bankName":"CNTS Niamey","scheduledAt":"2027-02-15T10:00:00"}'
```

### Service sans état (stateless)

Aucune donnée n'est gardée en mémoire entre deux requêtes (ni session, ni variable partagée) : tout est en base.
Preuve : redémarrer le conteneur (`docker compose restart donor-service`) et constater que les donneurs sont toujours là.

### Lancer le service seul (développement)

```bash
docker run -d --name donor-db -e POSTGRES_DB=donor_db -e POSTGRES_USER=donor \
  -e POSTGRES_PASSWORD=donor123 -p 5432:5432 postgres:16-alpine
export DB_PASSWORD=donor123
cd donor-service
mvn spring-boot:run
```

Variables d'environnement : `SERVER_PORT` (8081), `DB_URL` (`jdbc:postgresql://localhost:5432/donor_db`),
`DB_USER` (`donor`), `DB_PASSWORD` (obligatoire, sans valeur par défaut).

## Contrats

| Fichier | Contenu |
|---|---|
| `contracts/openapi.yaml` | API REST de donor-service (généré depuis le code, à régénérer à chaque changement d'API) |
| `contracts/stock.proto` | API gRPC de stock-service |
| `contracts/hospital.xsd`, `contracts/hospital.wsdl` | Contrat SOAP de hospital-service |

Pour régénérer le contrat OpenAPI (donor-service démarré) :

```bash
curl http://localhost:8081/v3/api-docs.yaml -o contracts/openapi.yaml
```

## Tests

```bash
cd donor-service && mvn test      # idem pour stock-service et hospital-service
```

donor-service : tests unitaires (règles R2 et R3, Mockito) et tests d'intégration de la couche REST
(MockMvc avec une base H2 en mémoire, profil `test`).

## Démo de résilience

> À COMPLÉTER par l'équipe : les 7 étapes de la démonstration de panne (circuit breaker, timeout, demande en attente).

## Équipe

| Membre | Contribution |
|---|---|
| Abdoul Wahab Mounahi Baguirbi (Membre 3) | donor-service (REST, CRUD, rendez-vous, règles R1-R3), contrat OpenAPI, README, rapport (parties 1, 2, 8, 9) |
| Membre 1 | À COMPLÉTER (api-gateway) |
| Membre 2 | À COMPLÉTER (docker-compose, stock-service) |
| Membre 4 | À COMPLÉTER (hospital-service, orchestration des demandes de sang) |
