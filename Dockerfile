# Imagen de ejecución: solo el JRE 17 sobre Alpine Linux.
# Se cambió desde eclipse-temurin:17-jre-jammy (Ubuntu) porque Snyk reportó 91 vulnerabilidades
# en los paquetes del sistema operativo; Alpine incluye muchos menos paquetes (menor superficie de ataque).
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

# Copia el artefacto generado por el job "build" del pipeline (mvn package -> target/banco2026.jar)
COPY target/banco2026.jar app.jar
RUN chown spring:spring app.jar
USER spring

# La JVM ajusta el heap al límite de memoria del contenedor
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
