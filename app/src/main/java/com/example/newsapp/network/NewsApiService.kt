package com.example.newsapp.network

// ВАЖЛИВО: Видаліть імпорт BuildConfig, якщо він є
// import com.example.newsapp.BuildConfig

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://newsapi.org/"

private const val API_KEY = "7aeab342139741abbed78df51932eb83"


private val authInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        // Використовуємо ключ з константи
        .addHeader("X-Api-Key", API_KEY)
        .build()
    chain.proceed(request)
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .build()

private val json = Json { ignoreUnknownKeys = true }

private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String,
        @Query("page") page: Int,       // +++ Номер сторінки
        @Query("pageSize") pageSize: Int // +++ Кількість елементів на сторінці
    ): NewsResponse
}

object NewsApi {
    val retrofitService: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }
}