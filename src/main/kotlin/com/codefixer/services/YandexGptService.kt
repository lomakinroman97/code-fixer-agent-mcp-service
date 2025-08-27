package com.codefixer.services

import com.codefixer.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

class YandexGptService(
    private val apiKey: String = System.getenv("YANDEX_GPT_API_KEY") ?: "",
    private val baseUrl: String = "https://llm.api.cloud.yandex.net"
) {
    private val logger = KotlinLogging.logger {}
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            })
        }
        
        // Увеличиваем таймауты для больших запросов
        install(HttpTimeout) {
            requestTimeoutMillis = TimeUnit.MINUTES.toMillis(5) // 5 минут на запрос
            connectTimeoutMillis = TimeUnit.SECONDS.toMillis(30) // 30 секунд на подключение
            socketTimeoutMillis = TimeUnit.MINUTES.toMillis(5) // 5 минут на сокет
        }
    }

    /**
     * Отправляет запрос к Yandex GPT для исправления кода
     */
    suspend fun fixCode(bugDescription: String, code: String, filePath: String): String? {
        return try {
            val prompt = buildPrompt(bugDescription, code, filePath)
            val request = createRequest(prompt)
            
            logger.info { "Sending request to Yandex GPT for file: $filePath" }
            logger.info { "Request URL: $baseUrl/foundationModels/v1/completion" }
            logger.info { "Code length: ${code.length} characters" }
            logger.info { "Prompt length: ${prompt.length} characters" }
            
            val response: YandexGptResponse = httpClient.post("$baseUrl/foundationModels/v1/completion") {
                header("Authorization", "Bearer $apiKey")
                header("Content-Type", "application/json")
                setBody(request)
            }.body()
            
            logger.info { "Received response from Yandex GPT" }
            logger.info { "Response result: ${response.result}" }
            logger.info { "Response alternatives count: ${response.result.alternatives.size}" }
            
            if (response.result.alternatives.isNotEmpty()) {
                val alternative = response.result.alternatives.first()
                logger.info { "First alternative: $alternative" }
                
                val message = alternative.message
                logger.info { "Message: $message" }
                
                val fixedCode = message.text
                if (fixedCode.isNotBlank()) {
                    logger.info { "Successfully extracted fixed code (${fixedCode.length} characters)" }
                    fixedCode
                } else {
                    logger.warn { "Fixed code is empty" }
                    null
                }
            } else {
                logger.warn { "No alternatives in response" }
                null
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Error calling Yandex GPT API: ${e.message}" }
            e.printStackTrace()
            null
        }
    }

    /**
     * Создает промпт для LLM согласно ТЗ
     */
    private fun buildPrompt(bugDescription: String, code: String, filePath: String): String {
        return """
            You are a senior software engineer. Fix the bug in the following code.
            Return ONLY the corrected code block in its entirety, without any explanations, comments, or surrounding text.
            Bug Description: $bugDescription
            Code to fix (File: $filePath)
            
            $code
        """.trimIndent()
    }

    /**
     * Создает запрос к Yandex GPT API
     */
    private fun createRequest(prompt: String): YandexGptRequest {
        return YandexGptRequest(
            modelUri = "gpt://b1gp9fidpabmov8j1rid/yandexgpt-lite",
            completionOptions = CompletionOptions(
                stream = false,
                temperature = 0.6,
                maxTokens = "2000",
                reasoningOptions = ReasoningOptions(mode = "DISABLED")
            ),
            messages = listOf(
                Message(
                    role = "system",
                    text = "Найди ошибки в тексте и исправь их"
                ),
                Message(
                    role = "user",
                    text = prompt
                )
            )
        )
    }

    /**
     * Закрывает HTTP клиент
     */
    fun close() {
        httpClient.close()
    }
}
