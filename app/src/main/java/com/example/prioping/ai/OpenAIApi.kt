package com.example.prioping.ai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApi {

    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun getAiResponse(
        @Header("Authorization") authHeader: String,
        @Body body: OpenAIApiRequestBody
    ): Response<OpenAIApiResponseBody>
}
