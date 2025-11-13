# Cloud Storage Service

Дипломный проект "Облачное хранилище"

## Запуск

```bash
docker-compose up --build
```

Логин/пароль

ivan / ivan


## Tests

Run unit and integration tests:

```bash
mvn test
```

Integration tests use Testcontainers (PostgreSQL). Ensure Docker is running for ITs.
