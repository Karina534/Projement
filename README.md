# Projemento

Projemento - монорепозиторий для системы управления проектами и задачами.

Корень репозитория предназначен для общей инфраструктуры: Docker Compose, общие переменные окружения и документация по запуску всех сервисов вместе. Код отдельных частей проекта лежит в отдельных папках.

## Структура репозитория

```text
Projement/
  README.md
  compose.yaml
  .env.example
  .gitignore
  .dockerignore

  backend-java/
    README.md
    Dockerfile
    pom.xml
    mvnw
    mvnw.cmd
    src/
    docs/

  backend-go/
    README.md
    Dockerfile
    go.mod
    ...

  frontend/
    README.md
    Dockerfile
    package.json
    ...
```

Сейчас реализован Java-бэкенд. Папки `backend-go` и `frontend` можно добавить позже, когда соответствующие части проекта появятся в репозитории.

## Документация сервисов

- [Java-бэкенд](backend-java/README.md)
- Go-бэкенд: будет добавлен позже
- Фронтенд: будет добавлен позже

## Быстрый запуск

Требования:

- Docker
- Docker Compose

Перед первым запуском можно создать локальный `.env` из примера:

```powershell
Copy-Item .env.example .env
```

В текущем репозитории `.env` уже содержит значения для локальной разработки. Этот файл не должен попадать в git.

Запустить все доступные сервисы:

```powershell
docker compose up -d --build
```

Проверить статус контейнеров:

```powershell
docker compose ps
```

Посмотреть логи Java-бэкенда:

```powershell
docker compose logs -f backend-java
```

Остановить контейнеры:

```powershell
docker compose down
```

Остановить контейнеры и удалить volume с базой данных:

```powershell
docker compose down -v
```

## Доступные адреса

Java-бэкенд:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

PostgreSQL с хоста:

```text
localhost:55432
```

## Переменные окружения

Docker Compose читает переменные из `.env`.

| Переменная | Назначение | Значение по умолчанию |
| --- | --- | --- |
| `POSTGRES_DB` | Имя базы PostgreSQL | `projemento` |
| `POSTGRES_USER` | Пользователь PostgreSQL | `projemento` |
| `POSTGRES_PASSWORD` | Пароль PostgreSQL | `projemento_password` |
| `POSTGRES_PORT` | Порт PostgreSQL на хосте | `55432` |
| `JAVA_BACKEND_PORT` | Порт Java-бэкенда на хосте | `8080` |
| `JWT_ACCESS_TOKEN_KEY` | JWK-секрет для access token | значение для разработки |
| `JWT_REFRESH_TOKEN_KEY` | JWK-секрет для refresh token | значение для разработки |

Для production нужно заменить пароли и JWT-секреты на уникальные значения.

## Docker Compose

Сейчас `compose.yaml` поднимает:

- `postgres` - PostgreSQL 16;
- `backend-java` - Spring Boot API из папки `backend-java`.

Когда появятся Go-бэкенд и фронтенд, их нужно добавить в тот же `compose.yaml` отдельными сервисами:

```yaml
services:
  backend-go:
    build:
      context: ./backend-go
      dockerfile: Dockerfile

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
```

## Полезные команды

Пересобрать только Java-бэкенд:

```powershell
docker compose build backend-java
```

Перезапустить только Java-бэкенд:

```powershell
docker compose restart backend-java
```

Открыть psql внутри контейнера PostgreSQL:

```powershell
docker compose exec postgres psql -U projemento -d projemento
```
