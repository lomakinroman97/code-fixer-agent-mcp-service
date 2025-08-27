package com.codefixer.services

import mu.KotlinLogging
import java.io.File

class FileService(
    private val rootDirectory: String = "/Users/lomakin_r/AndroidStudioProjects/chat_llm_code_helper/",
    private val maxFileSize: Int = 4000 // Максимальный размер файла в символах для API
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Читает файл по относительному пути и возвращает его содержимое
     */
    fun readFile(filePath: String): String? {
        return try {
            val absolutePath = File(rootDirectory, filePath)
            
            if (!absolutePath.exists()) {
                logger.warn { "File not found: $absolutePath" }
                return null
            }
            
            if (!absolutePath.isFile) {
                logger.warn { "Path is not a file: $absolutePath" }
                return null
            }
            
            val content = absolutePath.readText(Charsets.UTF_8)
            logger.info { "Successfully read file: $filePath (${content.length} characters)" }
            content
        } catch (e: Exception) {
            logger.error(e) { "Error reading file: $filePath" }
            null
        }
    }

    /**
     * Оптимизирует код для экономии токенов
     */
    fun optimizeCode(code: String): String {
        var optimized = code
            .removeComments()
            .removeExtraEmptyLines()
            .trim()
        
        // Если код слишком длинный, обрезаем его
        if (optimized.length > maxFileSize) {
            logger.warn { "Code too long (${optimized.length} chars), truncating to $maxFileSize chars" }
            optimized = optimized.take(maxFileSize) + "\n// ... (truncated due to size limit)"
        }
        
        return optimized
    }

    /**
     * Удаляет однострочные и многострочные комментарии
     */
    private fun String.removeComments(): String {
        // Удаляем многострочные комментарии /* ... */
        var result = this.replace(Regex("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/"), "")
        
        // Удаляем однострочные комментарии //
        result = result.replace(Regex("//.*$", RegexOption.MULTILINE), "")
        
        return result
    }

    /**
     * Удаляет лишние пустые строки
     */
    private fun String.removeExtraEmptyLines(): String {
        return this.replace(Regex("\n\\s*\n\\s*\n"), "\n\n")
    }

    /**
     * Проверяет существование файла
     */
    fun fileExists(filePath: String): Boolean {
        val absolutePath = File(rootDirectory, filePath)
        return absolutePath.exists() && absolutePath.isFile
    }
}
