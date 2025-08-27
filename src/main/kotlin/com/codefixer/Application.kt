package com.codefixer

import com.codefixer.routes.fixRoutes
import com.codefixer.services.CodeFixerService
import com.codefixer.services.FileService
import com.codefixer.services.YandexGptService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.serialization.kotlinx.json.*
import mu.KotlinLogging

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = KotlinLogging.logger {}
    
    // Инициализация сервисов
    val fileService = FileService()
    val yandexGptService = YandexGptService()
    val codeFixerService = CodeFixerService(fileService, yandexGptService)
    
    // Установка плагинов
    install(ContentNegotiation) {
        json()
    }
    
    // Настройка роутинга
    routing {
        fixRoutes(codeFixerService)
        
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }
    }
    
    // Graceful shutdown
    environment.monitor.subscribe(ApplicationStopping) {
        logger.info { "Application is stopping, closing services..." }
        yandexGptService.close()
    }
    
    logger.info { "CodeFixerAgent server started on port 8080" }
}
