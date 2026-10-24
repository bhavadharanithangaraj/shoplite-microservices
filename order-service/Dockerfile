# ---- Stage 1: build the jar with Maven ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Download dependencies first (cached until pom.xml changes)
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# The unit tests run here too, so a broken build never becomes an image
RUN mvn -B package

# ---- Stage 2: run the jar on a small JRE-only image ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --create-home spring
USER spring

COPY --from=build /app/target/order-service-*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
