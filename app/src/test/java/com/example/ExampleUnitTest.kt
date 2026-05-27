package com.example

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testGeminiApiCall() {
    val apiKey = "AIzaSyDM-7Xv3pEiaqYbdkH27c7DxVASObhrS4w"
    val testRequest = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = "السلام عليكم")))),
        systemInstruction = Content(parts = listOf(Part(text = "أنت مساعد لطيف اسمه نور.")))
    )
    try {
        val response = runBlocking {
            GeminiClient.apiService.generateContent(apiKey, testRequest)
        }
        println("Response: $response")
        assertNotNull(response)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        println("Assistant Response: $text")
        assertNotNull(text)
        assertTrue(text!!.isNotEmpty())
    } catch (e: Exception) {
        println("Direct API call exception: ${e.message}")
        if (e is retrofit2.HttpException) {
            println("Response Body: ${e.response()?.errorBody()?.string()}")
        }
        fail("Failed to call Gemini API: ${e.message}")
    }
  }
}
