package com.example.smishingdetectionapp.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class MessageRequest(val message: String)

data class TextCheckerResponse(
    val success: Boolean,
    val message: String,
    val messageIsSpam: Boolean,
    val reason: String
)

interface TextCheckerApi {
    @POST("/api/textChecker")
    fun checkMessage(@Body message: MessageRequest): Call<TextCheckerResponse>
}