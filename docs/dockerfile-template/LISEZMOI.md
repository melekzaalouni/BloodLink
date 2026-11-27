# Modèle de Dockerfile (Membres 1, 3 et 4)

1. Copiez `Dockerfile` et `.dockerignore` dans le dossier de votre service, à côté de `pom.xml`.
2. Changez uniquement la ligne `EXPOSE` : 8083 (hospital-service), 8081 (donor-service), 8080 (api-gateway).
3. Votre `application.yml` doit lire ces variables d'environnement (voir `docker-compose.yml`) :
   `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD` (sans valeur par défaut pour le mot de passe).
4. Votre service doit exposer `/actuator/health` (dépendance `spring-boot-starter-actuator`),
   car le healthcheck de Compose l'appelle avec `wget`.
5. Testez seul : `docker build -t bloodlink/<service> ./<service>`.
6. Donnez à Membre 2 le nom du service, son port et ses variables : il décommente votre bloc dans `docker-compose.yml`.

Pourquoi `-DskipTests` : l'image ne doit pas dépendre d'une base de données ; les tests tournent avec `mvn test`.
