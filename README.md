# Task Tracker

Простой трекер задач: доски, задачи со статусами и приоритетами, теги.

## Запуск

```bash
cp .env.example .env          # при желании поменяйте пароль и порты
docker compose up --build
```

Откройте http://localhost:3000.

Остановка: `docker compose down` (данные сохранятся в томе `pgdata`), `docker compose down -v` удалит и их.

## REST API

| Метод | Путь | Описание |
|---|---|---|
| GET | `/api/boards` | список досок (с количеством задач) |
| POST | `/api/boards` | создать доску |
| GET / PUT / DELETE | `/api/boards/{id}` | получить / изменить / удалить доску (задачи удаляются каскадно) |
| GET | `/api/boards/{id}/tasks?status=TODO` | задачи доски, фильтр по статусу необязателен |
| POST | `/api/boards/{id}/tasks` | создать задачу |
| GET / PUT / DELETE | `/api/tasks/{id}` | получить / изменить / удалить задачу |
| PATCH | `/api/tasks/{id}/status` | сменить только статус |
| GET / POST | `/api/tags` | список / создание тегов |
| GET / PUT / DELETE | `/api/tags/{id}` | получить / изменить / удалить тег |
| GET | `/api/info` | id экземпляра бэкенда |
| GET | `/actuator/health`, `/actuator/prometheus` | health-check и метрики |   

## Переменные окружения бэкенда

| Переменная | Обязательна | По умолчанию | Назначение |
|---|---|---|---|
| `DB_URL` | да | — | JDBC URL PostgreSQL |
| `DB_USER` | да | — | пользователь БД |
| `DB_PASSWORD` | да | — | пароль БД |
| `PORT` | нет | `8080` | HTTP-порт |
| `LOG_LEVEL` | нет | `INFO` | уровень логирования |

Фронтенд (nginx): `NGINX_PORT` (по умолчанию 80), `BACKEND_URL` (по умолчанию `http://backend:8080`).
