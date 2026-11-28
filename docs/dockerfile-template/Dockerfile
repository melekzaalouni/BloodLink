# MODÈLE DE DOCKERFILE BLOODLINK (identique pour les 4 services)
# Copie ce fichier à la racine de ton service (à côté de pom.xml) et change SEULEMENT la ligne EXPOSE :
#   hospital-service : 8083     donor-service : 8081     api-gateway : 8080
#
# ---------- stage 1 : build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Cache des dépendances tant que pom.xml ne change pas (étape facultative, d'où "|| true")
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- stage 2 : runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
