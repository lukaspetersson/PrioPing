package com.example.prioping.ai

data class OpenAIApiRequestBody(
    val model: String,
    val messages: List<Message>
) {
    data class Message(
        val role: String,
        val content: String
    )
}
