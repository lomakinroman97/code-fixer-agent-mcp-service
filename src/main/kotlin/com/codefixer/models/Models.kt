package com.codefixer.models

import kotlinx.serialization.Serializable

@Serializable
data class FixRequest(
    val file_path: String,
    val bug_description: String
)

@Serializable
data class FixResponse(
    val status: String,
    val fixed_code: String? = null,
    val message: String? = null
)

@Serializable
data class YandexGptRequest(
    val modelUri: String,
    val completionOptions: CompletionOptions,
    val messages: List<Message>
)

@Serializable
data class CompletionOptions(
    val stream: Boolean = false,
    val temperature: Double = 0.6,
    val maxTokens: String = "2000",
    val reasoningOptions: ReasoningOptions = ReasoningOptions()
)

@Serializable
data class ReasoningOptions(
    val mode: String = "DISABLED"
)

@Serializable
data class Message(
    val role: String,
    val text: String
)

@Serializable
data class YandexGptResponse(
    val result: Result
)

@Serializable
data class Result(
    val alternatives: List<Alternative>
)

@Serializable
data class Alternative(
    val message: Message
)
