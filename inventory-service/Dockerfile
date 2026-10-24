# ---- Stage 1: build the jar with Maven ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Download dependencies first (cached until pom.xml changes)
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# ---- Stage 2: run the jar on a small JRE-only image ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Don't run as root inside the container
RUN useradd --system --create-home spring
USER spring

COPY --from=build /app/target/inventory-service-*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
