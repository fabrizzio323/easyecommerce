
# ==========================================
# BUILD STAGE
# ==========================================
FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /app

# Copiar configuración Maven
COPY pom.xml .

# Descargar dependencias
RUN mvn -B -DskipTests dependency:resolve

# Copiar código fuente
COPY src ./src

# Compilar proyecto y generar JAR
RUN mvn -B -e clean package -DskipTests


# ==========================================
# RUNTIME STAGE
# ==========================================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copiar JAR generado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
