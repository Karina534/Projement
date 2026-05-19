# Projemento

Backend API for project management with users, JWT authentication, projects, boards, tasks and comments.

## Stack

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway migrations
- springdoc OpenAPI / Swagger UI
- Maven
- Docker Compose

## Quick Start With Docker

Requirements:

- Docker
- Docker Compose

Run the application with PostgreSQL:

```powershell
docker compose up -d --build
```

The application will be available at:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- PostgreSQL from host: `localhost:55432`

Check containers:

```powershell
docker compose ps
```

View application logs:

```powershell
docker compose logs -f app
```

Stop containers:

```powershell
docker compose down
```

Stop containers and remove the database volume:

```powershell
docker compose down -v
```

## Environment

Docker Compose reads variables from `.env`.

Local defaults are already provided in `.env` for development. For a new environment, copy `.env.example` to `.env` and change secrets:

```powershell
Copy-Item .env.example .env
```

Available variables:

| Variable | Description | Default |
| --- | --- | --- |
| `POSTGRES_DB` | PostgreSQL database name | `projemento` |
| `POSTGRES_USER` | PostgreSQL user | `projemento` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `projemento_password` |
| `POSTGRES_PORT` | Host port for PostgreSQL | `55432` |
| `APP_PORT` | Host port for the application | `8080` |
| `JWT_ACCESS_TOKEN_KEY` | JWK secret for access tokens | development value |
| `JWT_REFRESH_TOKEN_KEY` | JWK secret for refresh tokens | development value |

Use unique 256-bit JWK secrets in production.

## Local Run Without Docker

Start PostgreSQL separately or use only the database from Compose:

```powershell
docker compose up -d postgres
```

Run tests:

```powershell
.\mvnw test
```

Run the application locally:

```powershell
.\mvnw spring-boot:run
```

By default the local application connects to:

```text
jdbc:postgresql://localhost:5432/mydatabase
```

For Docker Compose, datasource settings are passed through environment variables and point to the `postgres` service.

## Database Migrations

Migrations are managed by Flyway and are applied automatically on application startup.

Migration files are stored in:

```text
src/main/resources/db/migration
```

## Useful Commands

Rebuild only the application image:

```powershell
docker compose build app
```

Restart the application:

```powershell
docker compose restart app
```

Open a PostgreSQL shell inside the database container:

```powershell
docker compose exec postgres psql -U projemento -d projemento
```
