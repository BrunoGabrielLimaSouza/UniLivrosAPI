FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

RUN echo "=== Listando arquivos gerados ===" && ls -lah /app/target/

RUN echo "=== Verificando JAR ===" && ls -lah /app/target/*.jar || echo "NENHUM JAR ENCONTRADO!"

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

EXPOSE 8088

COPY --from=build /app/target/*.jar app.jar

RUN echo "=== Verificando JAR copiado ===" && ls -lah app.jar

RUN echo "=== Testando integridade do JAR ===" && jar tf app.jar | head -20

ENTRYPOINT ["java","-jar","app.jar"]