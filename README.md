# File Processing Platform

Spring Boot service for authenticated file-processing jobs. The application accepts files through a REST API or from an input folder, sends file content through IBM MQ, converts XML payloads to JSON with Apache Camel, writes processed output files, and archives originals.

## What It Does

- Registers and authenticates users with JWT bearer tokens.
- Protects job and admin endpoints with Spring Security roles.
- Accepts file uploads through `/api/jobs/init-job-file`.
- Polls a configured input folder for XML files.
- Sends file content to IBM MQ.
- Converts XML messages to JSON.
- Writes JSON files to an output folder.
- Moves processed originals to an archive folder.
- Exposes OpenAPI/Swagger documentation.

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- IBM MQ JMS starter
- Apache Camel JMS routes
- JJWT
- Gradle
- H2 for tests

## Project Structure

```text
src/main/java/org/kos/fileprocessingplatform
  components/        Startup initializers and JWT utilities
  config/            Security, OpenAPI, MQ, mapper, and app configuration
  controllers/       REST API endpoints
  exceptions/        Global REST exception handling
  handler/           Folder polling, MQ sending, Camel MQ routes
  models/            JPA entities and DTOs
  repositories/      Spring Data repositories
  services/          Auth, current-user, and file-job services
  validation/        Register request password validation

src/main/resources
  application.yaml   Main application configuration

src/test/resources
  application-test.yaml  Self-contained test profile
```

## Requirements

- JDK 25
- Docker, if running IBM MQ locally
- PostgreSQL, if running the application with the default profile
- Gradle wrapper from this repository

## Configuration

The default runtime profile expects these environment variables:

| Variable | Purpose |
| --- | --- |
| `SQL_DATABASE_URL` | PostgreSQL JDBC URL, for example `jdbc:postgresql://localhost:5432/fileprocessing` |
| `SQL_DATABASE_USERNAME` | PostgreSQL username |
| `SQL_DATABASE_PASSWORD` | PostgreSQL password |
| `BASE_PATH` | Base directory for input, output, and archive folders |
| `JWT_SECRET` | HMAC signing secret for JWTs. Use at least 32 bytes for HS256. |
| `MQ_APP_PASSWORD` | IBM MQ app-user password |

The configured folders are derived from `BASE_PATH`:

```text
${BASE_PATH}/data/input
${BASE_PATH}/data/output
${BASE_PATH}/data/input/archive
```

## Local Development

### 1. Start IBM MQ

The compose file includes IBM MQ and Jenkins. To start only IBM MQ:

```bash
docker compose up -d ibm-mq
```

IBM MQ ports:

- `1414` for client connections
- `9443` for the MQ console

The compose file sets:

```text
MQ_QMGR_NAME=QM1
MQ_APP_PASSWORD=passw0rd
```

### 2. Start PostgreSQL

PostgreSQL is not currently included in `docker-compose.yaml`, so start it separately or point the app at an existing database.

Example connection values:

```text
SQL_DATABASE_URL=jdbc:postgresql://localhost:5432/fileprocessing
SQL_DATABASE_USERNAME=postgres
SQL_DATABASE_PASSWORD=postgres
```

### 3. Set Environment Variables

PowerShell example:

```powershell
$env:SQL_DATABASE_URL = "jdbc:postgresql://localhost:5432/fileprocessing"
$env:SQL_DATABASE_USERNAME = "postgres"
$env:SQL_DATABASE_PASSWORD = "postgres"
$env:BASE_PATH = "$PWD"
$env:JWT_SECRET = "replace-with-a-long-random-secret-at-least-32-bytes"
$env:MQ_APP_PASSWORD = "passw0rd"
```

### 4. Run the Application

```bash
./gradlew bootRun
```

On Windows:

```powershell
.\gradlew.bat bootRun
```

## Testing

Tests use the `test` Spring profile and an in-memory H2 database. They do not require PostgreSQL, IBM MQ, or production secrets.

```bash
./gradlew test
```

On Windows:

```powershell
.\gradlew.bat test
```

Run the full build:

```bash
./gradlew build
```

## API Overview

Swagger UI is available at:

```text
/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
/v3/api-docs
```

### Authentication

Register:

```http
POST /auth/api/v1/register
Content-Type: application/json
```

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "password123",
  "confirmPassword": "password123"
}
```

Login:

```http
POST /auth/api/v1/login
Content-Type: application/json
```

```json
{
  "loginString": "alice",
  "password": "password123"
}
```

Successful login returns a bearer token:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "userId": "user-uuid",
  "username": "alice",
  "email": "alice@example.com",
  "roles": []
}
```

Use the token on protected endpoints:

```http
Authorization: Bearer jwt-token
```

### Jobs

List jobs visible to the current user:

```http
GET /api/jobs
Authorization: Bearer jwt-token
```

Get one job. Job ids are numeric:

```http
GET /api/jobs/{id}
Authorization: Bearer jwt-token
```

Delete one job:

```http
DELETE /api/jobs/{id}
Authorization: Bearer jwt-token
```

Upload a file for processing:

```http
POST /api/jobs/init-job-file
Authorization: Bearer jwt-token
Content-Type: multipart/form-data
```

Multipart field:

```text
file
```

### Admin

```http
GET /admin/health
Authorization: Bearer admin-jwt-token
```

Requires `ROLE_ADMIN`.

## File Processing Flow

1. A user uploads a file through `/api/jobs/init-job-file`, or an XML file appears in the input folder.
2. `MqService` validates the file extension and sends the file content to the configured input queue.
3. `MqHandler` consumes from the input queue.
4. XML content is converted to JSON.
5. The converted JSON is sent to the output queue.
6. `MqHandler` consumes the output queue message.
7. JSON is written to the output folder.
8. The original input file is moved to the archive folder.

## Allowed File Types

Configured in `application.yaml`:

```yaml
app:
  file-processing:
    max-file-size-mb: 25
    allowed-extensions:
      - xml
```

Current conversion logic is XML-to-JSON, so XML is the only accepted upload/input extension by default.

## GitHub Actions

This repository includes two workflows:

- `.github/workflows/ci.yml`
- `.github/workflows/full-verification.yml`

Both run with `SPRING_PROFILES_ACTIVE=test`, so they build and test without requiring PostgreSQL, IBM MQ, or repository secrets.

## Production Readiness Notes

These items are important before using the project beyond local development:

- The default schema mode is non-destructive: `spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:validate}`. For a fresh local database, set `JPA_DDL_AUTO=update` or apply migrations before startup.
- No default admin account is created. Create admin users through a controlled operational process or a dedicated bootstrap path.
- File names are sanitized before filesystem writes, but deployments should still keep input/output/archive folders isolated from sensitive paths.
- `config.mqsc` and `application.yaml` are aligned on `JOBS.IN` and `JOBS.OUT`. Keep queue names synchronized if you change either file.
- Upload validation enforces both allowed extension and `max-file-size-mb`.
- Job endpoints use numeric ids matching `FileJobEntity.id`.
- Request DTO validation is active on authentication endpoints.

## Useful Commands

Build:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

Start IBM MQ:

```bash
docker compose up -d ibm-mq
```

Stop local compose services:

```bash
docker compose down
```

View workflow status in GitHub after pushing:

```bash
gh run list
```
