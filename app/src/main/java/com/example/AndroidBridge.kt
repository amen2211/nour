package com.example

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface

class AndroidBridge(private val context: Context) {

    @JavascriptInterface
    fun askGemini(msg: String): String {
        Log.d("AndroidBridge", "AI prompt: $msg")
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "عذراً، لم يتم تهيئة مفتاح API لمساعد نور. يرجى تهيئته عبر لوحة Secrets."
        }
        
        val systemInstruction = "أنت مساعد إسلامي متخصص اسمه \"نور\". تجيب على الأسئلة الإسلامية بعلم وأمانة. تستشهد بالآيات القرآنية والأحاديث الصحيحة من مصادرها المعتمدة. تكون مختصراً ومفيداً ولا تفتي في المسائل الخلافية الكبرى. اكتب بالعربية الفصحى الواضحة والجميلة والمنسقة."
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = msg)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )
        
        return try {
            val response = kotlinx.coroutines.runBlocking {
                GeminiClient.apiService.generateContent(apiKey, request)
            }
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            text ?: "عذراً، لم أتلقَّ رداً مفهوماً. هل يمكنك إعادة الصياغة؟"
        } catch (e: Exception) {
            Log.e("AndroidBridge", "Error calling Gemini: ${e.message}", e)
            "عذراً، واجهت مشكلة في معالجة طلبك: ${e.localizedMessage ?: e.message}"
        }
    }

    @JavascriptInterface
    fun schedulePrayerNotifications(prayerTimesJson: String) {
        Log.d("AndroidBridge", "Scheduling prayers: $prayerTimesJson")
        NotificationScheduler.schedulePrayerTimes(context, prayerTimesJson)
    }
}
