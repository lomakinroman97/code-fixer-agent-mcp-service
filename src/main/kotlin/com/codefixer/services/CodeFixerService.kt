package com.codefixer.services

import com.codefixer.models.FixRequest
import com.codefixer.models.FixResponse
import mu.KotlinLogging

class CodeFixerService(
    private val fileService: FileService,
    private val yandexGptService: YandexGptService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Основной метод для исправления кода
     */
    suspend fun fixCode(request: FixRequest): FixResponse {
        return try {
            logger.info { "Processing fix request for file: ${request.file_path}" }
            
            // Валидация входных данных
            if (request.file_path.isBlank()) {
                return FixResponse(
                    status = "error",
                    message = "File path cannot be empty"
                )
            }
            
            if (request.bug_description.isBlank()) {
                return FixResponse(
                    status = "error",
                    message = "Bug description cannot be empty"
                )
            }
            
            // Проверка существования файла
            if (!fileService.fileExists(request.file_path)) {
                return FixResponse(
                    status = "error",
                    message = "File not found: ${request.file_path}"
                )
            }
            
            // Чтение файла
            val originalCode = fileService.readFile(request.file_path)
            if (originalCode == null) {
                return FixResponse(
                    status = "error",
                    message = "Failed to read file: ${request.file_path}"
                )
            }
            
            // Оптимизация кода для экономии токенов
            val optimizedCode = fileService.optimizeCode(originalCode)
            logger.info { "Code optimized: ${originalCode.length} -> ${optimizedCode.length} characters" }
            
            // Отправка запроса к Yandex GPT
            val fixedCode = yandexGptService.fixCode(
                bugDescription = request.bug_description,
                code = optimizedCode,
                filePath = request.file_path
            )
            
            if (fixedCode != null) {
                logger.info { "Code successfully fixed for file: ${request.file_path}" }
                FixResponse(
                    status = "success",
                    fixed_code = fixedCode
                )
            } else {
                logger.error { "Failed to get fixed code from Yandex GPT for file: ${request.file_path}" }
                FixResponse(
                    status = "error",
                    message = "Failed to get fixed code from AI service"
                )
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error while processing fix request for file: ${request.file_path}" }
            FixResponse(
                status = "error",
                message = "Internal server error: ${e.message}"
            )
        }
    }
}
