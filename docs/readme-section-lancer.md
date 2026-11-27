## Lancer le projet

### Prérequis

Docker et Docker Compose uniquement (rien d'autre n'est nécessaire pour exécuter le projet).

### Démarrage

```bash
cp .env.example .env            # première fois seulement ; mettre ses propres mots de passe
docker compose up --build -d
docker compose ps               # attendre que tous les services soient "healthy"
```

Le premier build télécharge les dépendances et peut durer quelques minutes.

### Arrêt

```bash
docker compose down             # arrête en gardant les données (volumes nommés)
docker compose down -v          # arrête et efface aussi les données
```

### Tester stock-service (gRPC, port 9090)

Avec `grpcurl` installé :

```bash
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext -d '{"blood_type":"O-"}' localhost:9090 bloodlink.stock.v1.StockService/GetStock
grpcurl -plaintext -d '{"interval_seconds":1,"max_updates":2}' localhost:9090 bloodlink.stock.v1.StockService/StreamStockLevels
grpcurl -plaintext -d '{"blood_type":"ZZ"}' localhost:9090 bloodlink.stock.v1.StockService/GetStock
```

Résultats attendus : `GetStock` sur `O-` renvoie 4 unités avec `"urgent": true` ; le flux affiche
les 8 groupes deux fois ; `ZZ` donne `InvalidArgument`.

Sans installer `grpcurl` (Windows, via Docker) :

```powershell
docker run --rm fullstorydev/grpcurl -plaintext host.docker.internal:9090 list
'{"blood_type":"O-"}' | docker run --rm -i fullstorydev/grpcurl -plaintext -d '@' host.docker.internal:9090 bloodlink.stock.v1.StockService/GetStock
```

### Lancer les tests de stock-service

```bash
cd stock-service
mvn test
```

Les tests utilisent une base H2 en mémoire : ni PostgreSQL ni Docker ne sont nécessaires.
