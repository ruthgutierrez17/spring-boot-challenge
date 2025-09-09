# =========================
# Stage 1: Build
# =========================
FROM openjdk:21-jdk-slim as builder

# Información de la imagen
LABEL maintainer="angel.cc.710@outlook.com"
LABEL description="Challenge Backend API REST"
LABEL version="1.0.0"

# Directorio de trabajo
WORKDIR /app

# Copiar Maven Wrapper y archivos de configuración
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Convertimos mvnw a LF para evitar error /bin/sh: 1: ./mvnw: not found en Linux
RUN apt-get update && apt-get install -y dos2unix && dos2unix mvnw

# Dar permisos de ejecución al Maven Wrapper y descargar
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests


# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:21-jre

# Instalar curl para health checks
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Crear usuario no-root
RUN groupadd -r spring && useradd -r -g spring spring

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde el stage de build
COPY --from=builder /app/target/*.jar app.jar

# Crear directorio para logs
RUN mkdir -p /app/logs && chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring

# Variables de entorno para configuración
ENV SPRING_PROFILES_ACTIVE=docker

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
