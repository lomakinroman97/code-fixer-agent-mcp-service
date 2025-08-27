package com.codefixer.services

import mu.KotlinLogging
import java.io.File

class FileService(
    private val rootDirectory: String = System.getenv("CODEBASE_ROOT") ?: "/Users/lomakin_r/AndroidStudioProjects/chat_llm_code_helper/",
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
            .removeImports() // Новый этап: удаление импортов
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
     * Удаляет импорты для экономии токенов
     * LLM может работать без импортов, так как понимает контекст
     */
    private fun String.removeImports(): String {
        val lines = this.split("\n")
        val filteredLines = mutableListOf<String>()
        var inImportBlock = false
        var importBlockEnded = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // Начинаем блок импортов
            if (trimmedLine.startsWith("import ")) {
                inImportBlock = true
                continue // Пропускаем строку импорта
            }
            
            // Если мы в блоке импортов и встретили пустую строку, заканчиваем блок
            if (inImportBlock && trimmedLine.isEmpty()) {
                inImportBlock = false
                importBlockEnded = true
                continue // Пропускаем пустую строку после импортов
            }
            
            // Если мы в блоке импортов, продолжаем пропускать строки
            if (inImportBlock) {
                continue
            }
            
            // Если блок импортов закончился и встретили еще одну пустую строку, пропускаем
            if (importBlockEnded && trimmedLine.isEmpty()) {
                continue
            }
            
            // Сбрасываем флаг после первой непустой строки после импортов
            if (importBlockEnded && trimmedLine.isNotEmpty()) {
                importBlockEnded = false
            }
            
            // Добавляем строку, если она не импорт
            filteredLines.add(line)
        }
        
        return filteredLines.joinToString("\n")
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
