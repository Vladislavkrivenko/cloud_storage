
☁ Cloud Storage (Google Drive-like)

Многопользовательское файловое облако на Spring Boot.
Пользователи могут загать, скачивать, перемещать и хранить файлы в S3-совместимом хранилище MinIO.

🚀 Технологический стек

Java 17

Spring Boot 3

Spring Security

Spring Session (Redis)

Spring Data JPA

Liquibase

PostgreSQL

MinIO (S3 storage)

Redis (sessions)

Testcontainers

Docker / Docker Compose

Swagger (springdoc-openapi)

🏗 Архитектура

Приложение построено по слоистой архитектуре:

Controller → Facade → Service → Validator → Storage (MinIO / DB)

Основные компоненты:
🔐 Аутентификация

Spring Security

HttpSession

Redis хранит сессии

Cookie SESSION

👤 Пользователи

Хранятся в PostgreSQL

Пароли шифруются BCrypt

Миграции через Liquibase

📂 Файлы

Хранятся в MinIO (S3)

Один bucket: user-files

Каждый пользователь имеет root-папку:

user-{id}-files/


Пример:

user-1-files/docs/test.txt

🗂 Структура S3
user-files (bucket)
│
├── user-1-files/
│   ├── docs/
│   │   └── file.txt
│   └── photo.png
│
├── user-2-files/
│   └── report.pdf

🗄 Структура БД
Таблица: users
column	type	constraints
id	SERIAL	PRIMARY KEY
login	VARCHAR(50)	UNIQUE NOT NULL
password	VARCHAR(255)	NOT NULL

Индексы:

UNIQUE(login)

🐳 Запуск через Docker
1️⃣ docker-compose.yml

Запуск стека:

docker compose up -d


Поднимаются контейнеры:

Postgres

Redis

MinIO

Приложение

2️⃣ Остановка
docker compose down

3️⃣ Очистка volume
docker compose down -v

▶ Запуск приложения локально
mvn clean install
mvn spring-boot:run

🧪 Запуск тестов

Проект использует:

Testcontainers

PostgreSQL container

Redis container

MinIO container

Запуск:

mvn test


Тесты полностью поднимают:

Postgres

Redis

MinIO

🔐 REST API

Все API находятся под:

/api

📘 Swagger

После запуска приложение Swagger доступен по адресу:

http://localhost:8080/swagger-ui.html


OpenAPI JSON:

/v3/api-docs

👤 Работа с пользователями
Метод	Endpoint	Описание
POST	/api/auth/sign-up	Регистрация
POST	/api/auth/sign-in	Авторизация
POST	/api/auth/sign-out	Logout
GET	/api/user/me	Текущий пользователь
📁 Работа с файлами
Метод	Endpoint	Описание
POST	/api/resource	Upload
GET	/api/resource	Информация о ресурсе
DELETE	/api/resource	Удаление
GET	/api/resource/download	Скачать
GET	/api/resource/move	Move / Rename
GET	/api/resource/search	Поиск
GET	/api/directory	Содержимое папки
POST	/api/directory	Создать папку
🔐 Безопасность

Session-based authentication

Redis-backed sessions

Пользователь не может получить доступ к чужим файлам

CSRF отключён (REST API)

Максимум 1 активная сессия на пользователя

🧪 Покрытие тестами
AuthIntegrationTest

Регистрация

Авторизация

Logout

Проверка SESSION cookie

Проверка 401

FileIntegrationTest

Upload

Download

Move

Rename

Remove

Search

List directory

Zip download

IsolationIntegrationTest

Пользователь не видит чужие файлы

Нельзя скачать чужой файл

Нельзя удалить чужой файл

Нельзя переместить чужой файл

Нельзя искать чужие файлы

📦 Production-ready особенности

Liquibase миграции

Redis для session storage

S3-совместимое файловое хранилище

Полная изоляция пользователей

Docker-ready

Swagger документация

Интеграционные тесты
