# 🔐 OTP Service

Сервис для генерации и проверки одноразовых кодов (OTP) с отправкой в Telegram, SMS (эмуляция), Email (эмуляция) и сохранением в файл.

## 🛠 Технологии

- Java 17
- PostgreSQL 17
- Gradle
- JWT для аутентификации
- BCrypt для хэширования паролей
- Telegram Bot API

## 🚀 Установка и запуск

### Требования
- Java 17
- PostgreSQL 17

### Шаг 1: Клонирование репозитория

```bash
git clone https://github.com/myagenkiy/Otp-service.git
cd otp-service

### Шаг 2: Настройка базы данных
Выполните скрипт init.sql в PostgreSQL:

bash
psql -U postgres -f init.sql

### Шаг 3: Настройка конфигурации
Скопируйте файл настроек:

bash
cp .env.example .env
Откройте .env и укажите свой пароль PostgreSQL:

env
DB_PASSWORD=ВАШ_ПАРОЛЬ

### Шаг 4: Запуск
В IntelliJ IDEA: Запустите Application.java

После запуска откроется консольное меню для тестирования.

📋 API Endpoints
Метод	Endpoint	Описание
POST	/api/register	Регистрация пользователя
POST	/api/login	Вход, получение JWT токена
POST	/api/user/generate	Генерация OTP кода
POST	/api/user/validate	Проверка OTP кода
GET	/api/admin/users	Список пользователей (admin)
PUT	/api/admin/config	Обновление конфигурации (admin)
DELETE	/api/admin/users/delete	Удаление пользователя (admin)

### Настройка Telegram (опционально)
Создайте бота у @BotFather

Получите токен и chat_id

Добавьте в .env:

env
TELEGRAM_BOT_TOKEN=ваш_токен
TELEGRAM_CHAT_ID=ваш_chat_id

### Структура проекта

otp-service/
│
├── .env                          # Конфигурация
├── .env.example                  # Шаблон конфигурации
├── .gitignore                    # Git ignore
├── build.gradle                  # Сборка Gradle
├── init.sql                      # SQL скрипт для БД
├── README.md                     # Документация
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── promo/
│       │           └── otp/
│       │               │
│       │               ├── Application.java              # Точка входа
│       │               │
│       │               ├── config/
│       │               │   ├── ConfigLoader.java         # Загрузка .env
│       │               │   └── DatabaseConfig.java       # Настройка БД
│       │               │
│       │               ├── controller/
│       │               │   ├── AdminController.java      # API админа
│       │               │   ├── AuthController.java       # API регистрации/логина
│       │               │   └── UserController.java       # API пользователя
│       │               │
│       │               ├── dao/
│       │               │   ├── ConfigDao.java            # Работа с config
│       │               │   ├── OtpDao.java               # Работа с otp_codes
│       │               │   └── UserDao.java              # Работа с users
│       │               │
│       │               ├── model/
│       │               │   ├── Config.java               # Модель конфигурации
│       │               │   ├── OtpCode.java              # Модель OTP кода
│       │               │   └── User.java                 # Модель пользователя
│       │               │
│       │               ├── security/
│       │               │   ├── AuthFilter.java           # Проверка JWT
│       │               │   ├── JwtUtil.java              # Работа с JWT
│       │               │   └── PasswordUtil.java         # Хэширование паролей
│       │               │
│       │               ├── service/
│       │               │   ├── AuthService.java          # Логика авторизации
│       │               │   ├── ChannelSender.java        # Интерфейс отправки
│       │               │   ├── EmailSender.java          # Email эмуляция
│       │               │   ├── ExpiredCodesCleaner.java  # Очистка кодов
│       │               │   ├── FileSender.java           # Сохранение в файл
│       │               │   ├── OtpService.java           # Логика OTP
│       │               │   ├── SmsSenderMock.java        # SMS эмуляция
│       │               │   └── TelegramSender.java       # Отправка в Telegram
│       │               │
│       │               └── utils/
│       │                   └── JsonUtil.java             # Работа с JSON
│       │
│       └── resources/
│           └── (пусто)
│
└── gradle/
    └── wrapper/
        ├── gradle-wrapper.jar
        └── gradle-wrapper.properties
