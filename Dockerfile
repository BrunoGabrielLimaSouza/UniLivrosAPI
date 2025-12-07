FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean install -DskipTests


FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

EXPOSE 8088

COPY --from=build /app/target/unilivros-api-0.0.1-SNAPSHOT.jar app.jar/

ENTRYPOINT ["java", "-jar", "app.jar"]