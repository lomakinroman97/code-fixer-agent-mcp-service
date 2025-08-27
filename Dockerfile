# Используем современный OpenJDK 17 образ
FROM eclipse-temurin:17-jre-jammy

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Копируем JAR файл
COPY build/libs/*.jar app.jar

# Создаем директории для логов и кода
RUN mkdir -p /app/logs /app/codebase

# Устанавливаем права доступа
RUN chown -R appuser:appuser /app

# Переключаемся на пользователя приложения
USER appuser

# Открываем порт
EXPOSE 8080

# Устанавливаем переменные окружения
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Команда запуска
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
