# Banco 2026 — API REST con CI/CD

<!-- BADGES: se agregan a medida que se integra cada plataforma -->

API REST bancaria construida con **Spring Boot** sobre una arquitectura en capas (Laboratorio 1), integrada en el Laboratorio 2 a un pipeline de **Integración y Despliegue Continuo (CI/CD)**.

**Operaciones disponibles:**
* Gestión de clientes (crear, consultar, actualizar, eliminar)
* Transferencias entre cuentas (crear, consultar, actualizar, anular)
* Documentación interactiva con Swagger UI
* Autenticación HTTP Basic con roles `USER` (lectura) y `ADMIN` (escritura)

Incluye integración con **GitHub Actions**, **SonarCloud** (calidad), **JaCoCo** (cobertura), **Docker Hub** (imagen), **Render** (despliegue en la nube) y **Snyk** (seguridad).

> 📄 El paso a paso del laboratorio se encuentra en [INFORME-LAB2.md](INFORME-LAB2.md).

**Estudiante:** Mateo Vargas Tirado · **Asignatura:** Arquitectura de Software · **UdeA** · 2026-2

---

### Estructura de carpetas
* `src/main/java` — código principal de la aplicación (controller, service, repository, entity, DTO, mapper, config, exception)
* `src/main/resources` — configuración (`application.properties`)
* `src/test/java` — pruebas unitarias y de integración
* `.github/workflows` — definición del pipeline CI/CD
* `doc` — diagramas e imágenes del informe

### Cómo instalarlo y ejecutarlo
Requisitos: JDK 17+ y una instancia de MySQL (base de datos `banco2026`).
```shell
./mvnw spring-boot:run
```
Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Cómo ejecutar las pruebas
```shell
./mvnw clean install
```

### Cómo obtener el reporte de cobertura
```shell
./mvnw clean verify
```
El reporte de JaCoCo queda en `target/site/jacoco/index.html`.

### Despliegue en la nube
_Pendiente._
