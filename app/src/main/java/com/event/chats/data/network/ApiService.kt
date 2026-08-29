package com.event.chats.data.network

import com.event.chats.data.network.model.GeminiRequest
import com.event.chats.data.network.model.GeminiResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
   @POST("v1beta/models/gemini-3.5-flash-lite:generateContent")
   suspend fun generateContent(
       @Header("x-goog-api-key")apiKey: String,
       @Body request: GeminiRequest
   ): GeminiResponse

  @Streaming
  @POST("v1beta/models/gemini-3.5-flash-lite:streamGenerateContent")
  suspend fun streamGenerateContent(
      @Header("x-goog-api-key")apiKey: String,
      @Query("alt") alt: String = "sse",
      @Body request: GeminiRequest
  ): Response<ResponseBody>
}