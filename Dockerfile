FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Limpa cache do Maven e força rebuild
RUN mvn dependency:purge-local-repository clean package -DskipTests -U

RUN echo "=== JAR gerado ===" && ls -lah /app/target/*.jar

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

EXPOSE 8088

COPY --from=build /app/target/unilivros-api.jar app.jar/

ENTRYPOINT ["java","-jar","app.jar"]