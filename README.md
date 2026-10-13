# Banco 2026 — API REST con CI/CD

[![CI/CD Pipeline](https://github.com/AraiGumaDev/back-lab02-banco2026-arqsoft-udea/actions/workflows/build.yml/badge.svg)](https://github.com/AraiGumaDev/back-lab02-banco2026-arqsoft-udea/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=araigumadevprojects_lab02-banco2026-arqsoft-udea&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=araigumadevprojects_lab02-banco2026-arqsoft-udea)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=araigumadevprojects_lab02-banco2026-arqsoft-udea&metric=coverage)](https://sonarcloud.io/summary/new_code?id=araigumadevprojects_lab02-banco2026-arqsoft-udea)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=araigumadevprojects_lab02-banco2026-arqsoft-udea&metric=bugs)](https://sonarcloud.io/summary/new_code?id=araigumadevprojects_lab02-banco2026-arqsoft-udea)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=araigumadevprojects_lab02-banco2026-arqsoft-udea&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=araigumadevprojects_lab02-banco2026-arqsoft-udea)
<!-- BADGES: se agregan a medida que se integra cada plataforma -->

API REST bancaria construida con **Spring Boot** sobre una arquitectura en capas (Laboratorio 1), integrada en el Laboratorio 2 a un pipeline de **Integración y Despliegue Continuo (CI/CD)**.

**Operaciones disponibles:**
* Gestión de clientes (crear, consultar, actualizar, eliminar)
* Transferencias entre cuentas (crear, consultar, actualizar, anular)
* Documentación interactiva con Swagger UI
* Autenticación HTTP Basic con roles `USER` (lectura) y `ADMIN` (escritura)

Incluye integración con **GitHub Actions**, **SonarCloud** (calidad), **JaCoCo** (cobertura), **Docker Hub** (imagen), **Render** (despliegue en la nube) y **Snyk** (seguridad).

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
Las pruebas usan una base de datos H2 en memoria (`src/test/resources/application.properties`), por lo que **no requieren MySQL**.

### Variables de entorno
| Variable | Descripción | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP | `8080` |
| `APP_DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/banco2026?...` |
| `APP_DB_USER` / `APP_DB_PASSWORD` | Credenciales de la BD | `banco_user` / `user123` |
| `USER_PASSWORD` / `ADMIN_PASSWORD` | Claves de los usuarios `user` y `admin` | `root` |

### Cómo obtener el reporte de cobertura
```shell
./mvnw clean verify
```
El reporte de JaCoCo queda en `target/site/jacoco/index.html`.

### Despliegue en la nube
_Pendiente._
