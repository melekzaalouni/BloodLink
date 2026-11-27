# ---------- stage 1 : build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Mise en cache des dépendances tant que pom.xml ne change pas.
# "|| true" : cette étape n'est qu'un cache, la vraie compilation suit et affichera toute erreur réelle.
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- stage 2 : runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 9090 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
