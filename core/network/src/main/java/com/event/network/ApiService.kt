package com.event.network

import com.event.network.model.GeminiRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    @Streaming
    @POST("v1beta/models/gemini-3.5-flash-lite:streamGenerateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Query("alt") alt:  String ="sse",
        @Body request: GeminiRequest
    ): Response<ResponseBody>
}