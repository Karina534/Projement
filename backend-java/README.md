# Java-бэкенд

Java-бэкенд - это Spring Boot сервис проекта Projemento. Он отвечает за пользователей, JWT-авторизацию, проекты, доски, задачи и комментарии.

## Стек

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- springdoc OpenAPI / Swagger UI
- Maven

## Что реализовано

- Регистрация пользователя.
- Получение текущего пользователя.
- JWT access token и refresh token.
- Logout с деактивацией refresh token.
- Проекты и участники проектов.
- Kanban-доска проекта с колонками.
- Задачи, смена статуса, назначение исполнителя, дедлайн и приоритет.
- Комментарии к задачам.
- Flyway-миграции БД.
- Swagger/OpenAPI.
- Интеграционные тесты на H2.

## Запуск через общий Docker Compose

Из корня репозитория:

```powershell
docker compose up -d --build
```

Java API будет доступен по адресу:

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

Логи Java-сервиса:

```powershell
docker compose logs -f backend-java
```

## Локальный запуск без контейнера приложения

Можно поднять только PostgreSQL из корня репозитория:

```powershell
docker compose up -d postgres
```

Затем в папке `backend-java` указать подключение к базе и запустить приложение:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:55432/projemento"
$env:SPRING_DATASOURCE_USERNAME="projemento"
$env:SPRING_DATASOURCE_PASSWORD="projemento_password"
$env:JWT_ACCESS_TOKEN_KEY='{"kty":"oct","k":"asfgMthWBltjLyJn791pD38LKqirGhI8dWYEh_hzrWI"}'
$env:JWT_REFRESH_TOKEN_KEY='{"kty":"oct","k":"HWS6epvQPr1Y71__89JrI66ONrXJLQOnJT4vaR0OQjM"}'
.\mvnw spring-boot:run
```

## Тесты

В папке `backend-java`:

```powershell
.\mvnw test
```

Если Maven Wrapper в PowerShell не запускается из-за локальных ограничений окружения, можно использовать установленный Maven:

```powershell
mvn test
```

## Миграции

Миграции находятся здесь:

```text
backend-java/src/main/resources/db/migration
```

Flyway применяет миграции автоматически при старте приложения.

Текущие миграции:

- `V1__init.sql` - пользователи и деактивированные JWT-токены.
- `V2__task_tracker.sql` - проекты, участники, доски, колонки, задачи и комментарии.

## Docker

Dockerfile сервиса находится в:

```text
backend-java/Dockerfile
```

Образ собирается многостадийно:

- build stage: Maven + JDK 21;
- runtime stage: JRE 21 Alpine;
- запуск выполняется не от root-пользователя;
- используется Spring Boot layered extraction для лучшего кеширования слоев.

Пересобрать только Java-бэкенд:

```powershell
docker compose build backend-java
```
