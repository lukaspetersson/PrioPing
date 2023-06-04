package com.example.prioping.ai

data class OpenAIApiResponseBody(
    val choices: List<Choice>
) {
    data class Choice(
        val message: Message
    ) {
        data class Message(
            val content: String
        )
    }
}
