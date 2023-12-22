FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app
COPY . /app

# Ensure the Maven Wrapper is executable
RUN chmod +x mvnw

RUN ./mvnw install

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/quarkus-app/ /app/quarkus-app/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/quarkus-app/quarkus-run.jar"]
