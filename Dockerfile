FROM openjdk:17-jre-slim

WORKDIR /app

# Копируем JAR файл
COPY build/libs/code-fixer-agent-1.0.0.jar app.jar

# Создаем директорию для логов
RUN mkdir -p logs

# Открываем порт
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]
