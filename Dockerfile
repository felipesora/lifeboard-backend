# Etapa de build: Java 21 + Maven
FROM eclipse-temurin:21-jdk AS build

# Instala o Maven
RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Executa o build
RUN mvn clean install -DskipTests

# Etapa de runtime: imagem menor só com Java
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]