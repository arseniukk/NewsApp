package com.example.newsapp.network

import com.example.newsapp.rss.RssFeed
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET

private val retrofitRss = Retrofit.Builder()
    .baseUrl("https://www.pravda.com.ua/")
    .addConverterFactory(SimpleXmlConverterFactory.create())
    .build()

interface RssApiService {
    @GET("rss/")
    suspend fun getUkrainianNews(): RssFeed
}

object RssApi {
    val retrofitService: RssApiService by lazy {
        retrofitRss.create(RssApiService::class.java)
    }
}