package ai.turkuaz.music.data.api

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Emulator'da backend'e ulasmak icin 10.0.2.2 kullanilir (localhost -> host makine).
    // Gercek cihazda ayni Wi-Fi'daki bilgisayarin IP'si veya deploy edilmis URL yazilmali.
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private lateinit var sessionManager: SessionManager

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { sessionManager.getToken() }
        val request = if (token != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /** Media (ses/kapak) dosyalari icin taban URL, gorece file_url'lerle birlestirilir. */
    fun mediaUrl(relativePath: String): String = BASE_URL.trimEnd('/') + relativePath
}
