# Imagen de ejecución: solo el JRE 17 (más liviana que un JDK completo)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

# Copia el artefacto generado por el job "build" del pipeline (mvn package -> target/banco2026.jar)
COPY target/banco2026.jar app.jar
RUN chown spring:spring app.jar
USER spring

# La JVM ajusta el heap al límite de memoria del contenedor
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
