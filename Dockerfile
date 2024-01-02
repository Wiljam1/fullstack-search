FROM maven:3.8.1-openjdk-17-slim AS build
COPY src /app/src
COPY pom.xml /app
RUN mvn -f app/pom.xml install

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/quarkus-app/ /app/quarkus-app/
EXPOSE 8080
ENV QUARKUS_OIDC_AUTH_SERVER_URL=https://keycloak.app.cloud.cbh.kth.se/realms/patient-keycloak
ENTRYPOINT ["java", "-jar", "/app/quarkus-app/quarkus-run.jar"]