# -------- BUILD STAGE --------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:resolve

# Copy source
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar jar específico (más seguro)
COPY --from=build /app/target/*jar /app/app.jar

EXPOSE 8081

# JVM más flexible (mejor para cloud)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]