package com.example

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface

class AndroidBridge(private val context: Context) {

    @JavascriptInterface
    fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    @JavascriptInterface
    fun askGemini(msg: String): String {
        Log.d("AndroidBridge", "AI prompt: $msg")
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "PUT_YOUR_API_KEY_HERE") {
            return "عذراً، لم يتم تهيئة مفتاح API لمساعد نور. يرجى تهيئته عبر لوحة Secrets."
        }
        
        val systemInstruction = "أنت مساعد إسلامي متخصص اسمه \"نور\". تجيب على الأسئلة الإسلامية بعلم وأمانة. تستشهد بالآيات القرآنية والأحاديث الصحيحة من مصادرها المعتمدة. تكون مختصراً ومفيداً ولا تفتي في المسائل الخلافية الكبرى. اكتب بالعربية الفصحى الواضحة والجميلة والمنسقة."
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = msg)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )
        
        return try {
            val response = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
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
    fun copyToClipboard(text: String): Boolean {
        Log.d("AndroidBridge", "copyToClipboard called")
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Nour Copy", text)
            var success = true
            (context as? android.app.Activity)?.runOnUiThread {
                try {
                    clipboard.setPrimaryClip(clip)
                } catch (se: SecurityException) {
                    Log.e("AndroidBridge", "SecurityException copying text natively in runOnUiThread: ${se.message}")
                } catch (e: Exception) {
                    Log.e("AndroidBridge", "Exception copying text natively in runOnUiThread: ${e.message}")
                }
            } ?: run {
                try {
                    clipboard.setPrimaryClip(clip)
                } catch (se: SecurityException) {
                    Log.e("AndroidBridge", "SecurityException copying text natively: ${se.message}")
                    success = false
                } catch (e: Exception) {
                    Log.e("AndroidBridge", "Exception copying text natively: ${e.message}")
                    success = false
                }
            }
            success
        } catch (e: Exception) {
            Log.e("AndroidBridge", "Error copying text natively: ${e.message}", e)
            false
        }
    }

    @JavascriptInterface
    fun schedulePrayerNotifications(prayerTimesJson: String) {
        Log.d("AndroidBridge", "Scheduling prayers: $prayerTimesJson")
        NotificationScheduler.schedulePrayerTimes(context, prayerTimesJson)
    }
}
