package com.codefixer.routes

import com.codefixer.models.FixRequest
import com.codefixer.services.CodeFixerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging

fun Route.fixRoutes(codeFixerService: CodeFixerService) {
    val logger = KotlinLogging.logger {}
    
    route("/api") {
        post("/fix") {
            try {
                logger.info { "Received fix request" }
                
                val request = call.receive<FixRequest>()
                logger.info { "Request: file_path=${request.file_path}, bug_description=${request.bug_description}" }
                
                val response = codeFixerService.fixCode(request)
                
                when (response.status) {
                    "success" -> {
                        call.respond(HttpStatusCode.OK, response)
                        logger.info { "Successfully processed fix request for file: ${request.file_path}" }
                    }
                    "error" -> {
                        call.respond(HttpStatusCode.BadRequest, response)
                        logger.warn { "Error processing fix request: ${response.message}" }
                    }
                    else -> {
                        call.respond(HttpStatusCode.InternalServerError, response)
                        logger.error { "Unknown status in response: ${response.status}" }
                    }
                }
                
            } catch (e: Exception) {
                logger.error(e) { "Error processing fix request" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "status" to "error",
                        "message" to "Internal server error: ${e.message}"
                    )
                )
            }
        }
    }
}
