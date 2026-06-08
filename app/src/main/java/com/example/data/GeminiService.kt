package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: GeminiApi = retrofit.create(GeminiApi::class.java)
}

object GeminiRepository {
    private const val TAG = "GeminiRepository"

    suspend fun generateFinancialTip(goal: SavingGoal?): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is placeholder or blank.")
            return@withContext getFallbackTip(goal)
        }

        val prompt = if (goal != null) {
            val progressPercent = ((goal.savedAmount / goal.price) * 100).toInt()
            val remaining = goal.price - goal.savedAmount
            val deadlineStr = if (goal.targetDate != null) {
                val diffMills = goal.targetDate - System.currentTimeMillis()
                val daysLeft = (diffMills / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                "dengan tenggat waktu $daysLeft hari lagi"
            } else {
                "tanpa tenggat waktu spesifik"
            }

            """
            Berikan satu tips keuangan harian yang dipersonalisasi dan sangat memotivasi dalam Bahasa Indonesia (maksimal 2 kalimat pendek).
            Target pengguna saat ini: Menabung untuk "${goal.name}".
            Total harga target: Rp ${String.format("%,.0f", goal.price)}.
            Sudah terkumpul: Rp ${String.format("%,.0f", goal.savedAmount)} ($progressPercent% dari target).
            Kurang: Rp ${String.format("%,.0f", remaining)} $deadlineStr.
            Tips harus praktis, kreatif, cerdas, ramah, dan langsung berhubungan dengan pencapaian target tersebut atau cara menyisihkan uang harian ekstra.
            """.trimIndent()
        } else {
            "Berikan satu tips keuangan harian universal yang cerdas, praktis, ramah, dan memotivasi dalam Bahasa Indonesia (maksimal 2 kalimat pendek) untuk membantu sahabat mulai menabung barang impian mereka."
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        try {
            val response = GeminiClient.apiService.generateContent(apiKey, request)
            val tip = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!tip.isNullOrBlank()) {
                tip.trim()
            } else {
                getFallbackTip(goal)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Gemini API: ${e.message}", e)
            getFallbackTip(goal)
        }
    }

    private fun getFallbackTip(goal: SavingGoal?): String {
        return if (goal != null) {
            val progressPercent = ((goal.savedAmount / goal.price) * 100).toInt()
            when {
                progressPercent >= 100 -> "Target ${goal.name} kamu sudah tercapai! Saatnya merayakan pencapaian luar biasa ini dan tentukan target impian baru selanjutnya! 🎉"
                progressPercent >= 75 -> "Wah, sisa sedikit lagi target ${goal.name} tercapai! Coba kurangi satu jajan kopi atau cemilan minggu ini untuk menambah tabungan akhirmu. Kamu pasti bisa! 🚀"
                progressPercent >= 50 -> "Hebat! Kamu sudah berjalan setengah jalan menuju ${goal.name}. Konsistensi adalah kunci keuangan yang sehat, teruskan semangat menabungmu! 🌱"
                progressPercent >= 25 -> "Progress menabung ${goal.name}-mu berjalan lancar (sudah $progressPercent%). Coba cari barang bekas berkualitas atau tantangan weekend hemat untuk mempercepat terkumpulnya sisa tabungan!"
                else -> "Langkah pertama adalah yang tersulit, namun kamu telah memulai perjalanan menabung untuk ${goal.name}! Sisihkan nominal kecil secara konsisten setiap hari demi masa depan impianmu. 💪"
            }
        } else {
            "Mulailah dengan membuat target kecil di tab Wishlist. Menabung Rp 10.000 sehari secara konsisten jauh lebih baik daripada menabung nominal besar sekali-kali! 🪙"
        }
    }
}
