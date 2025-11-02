package com.example.newsapp

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object LocationUtils {

    // Розширений словник країн та великих міст.
    private val knownLocations = setOf(
        // Країни
        "USA", "United States", "China", "Ukraine", "Germany", "France", "UK", "United Kingdom",
        "Japan", "India", "Canada", "Brazil", "Italy", "Spain", "Russia", "Australia",
        "Mexico", "Argentina", "Poland", "Turkey", "Iran", "Iraq", "Syria", "Israel", "Egypt",
        "South Korea", "Nigeria", "Pakistan", "Afghanistan", "Taiwan",

        // Міста
        "New York", "London", "Paris", "Tokyo", "Beijing", "Kyiv", "Berlin", "Moscow",
        "Washington", "Los Angeles", "Chicago", "Toronto", "Sydney", "Rome", "Madrid",
        "Jerusalem", "Gaza", "Tehran", "Baghdad", "Damascus"
    )

    /**
     * Шукає першу відому геолокацію в тексті статті (публічна функція).
     */
    fun findFirstLocationInText(article: Article): String? {
        val textToSearch = "${article.title} ${article.description}"
        // Шукаємо перше слово зі словника, яке зустрічається в тексті
        return knownLocations.find { location ->
            textToSearch.contains(location, ignoreCase = true)
        }
    }

    /**
     * Асинхронно отримує координати для статті (публічна функція).
     */
    suspend fun getLatLngFromArticle(context: Context, article: Article): LatLng? {
        val locationName = findFirstLocationInText(article) ?: return null

        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context)

        return withContext(Dispatchers.IO) { // Виконуємо в фоновому потоці
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(locationName, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    Log.d("Geocoding", "Знайдено координати для '$locationName': ${address.latitude}, ${address.longitude}")
                    LatLng(address.latitude, address.longitude)
                } else {
                    Log.w("Geocoding", "Не знайдено координат для '$locationName'")
                    null
                }
            } catch (e: IOException) {
                Log.e("Geocoding", "Помилка Geocoder", e)
                null
            }
        }
    }
}