# CodeFixerAgent - Быстрый старт

## 🚀 Запуск за 3 шага

### 1. Сборка проекта
```bash
./gradlew build
```

### 2. Запуск сервера
```bash
./gradlew run
```

### 3. Тестирование API
```bash
curl -X POST http://localhost:8080/api/fix \
  -H "Content-Type: application/json" \
  -d '{
    "file_path": "app/src/main/java/com/example/chat_llm_code_helper/ui/components/LoadingIndicator.kt",
    "bug_description": "Проверка работы API"
  }'
```

## ✅ Что работает

- HTTP API на порту 8080
- Интеграция с Yandex GPT
- Оптимизация кода (удаление комментариев)
- Обработка файлов до 4000 символов
- Health check: `GET /health`

## 🔧 Конфигурация

- **Кодовая база**: `/Users/lomakin_r/AndroidStudioProjects/chat_llm_code_helper/`
- **API ключ**: Встроен в код
- **Таймауты**: 5 минут на запрос

## 📁 Структура

```
src/main/kotlin/com/codefixer/
├── models/          # API модели
├── services/        # Бизнес-логика
├── routes/          # HTTP роуты
└── Application.kt   # Точка входа
```

## 🐳 Docker

```bash
docker build -t codefixer-agent .
docker run -p 8080:8080 -v /Users/lomakin_r/AndroidStudioProjects/chat_llm_code_helper:/Users/lomakin_r/AndroidStudioProjects/chat_llm_code_helper:ro codefixer-agent
```

## 📝 Логи

- Консоль: STDOUT
- Файл: `logs/codefixer.log`
- Уровень: INFO
