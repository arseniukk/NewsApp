package com.example.newsapp

import com.example.newsapp.network.ArticleDto
import com.example.newsapp.network.NewsApi
import java.text.SimpleDateFormat
import java.util.Locale

class NewsRepository {

    suspend fun getTopHeadlines(): List<Article> {
        val response = NewsApi.retrofitService.getTopHeadlines(country = "us")
        // Конвертуємо DTO в нашу модель Article
        return response.articles.mapNotNull { it.toArticle() }
    }
}

// Функція-маппер для конвертації
private fun ArticleDto.toArticle(): Article? {
    // Відкидаємо статті без заголовку або опису
    if (title.isNullOrEmpty() || description.isNullOrEmpty()) {
        return null
    }

    return Article(
        // Генеруємо унікальний ID на основі URL, оскільки API не надає ID
        id = url?.hashCode() ?: title.hashCode(),
        title = title,
        description = description,
        author = author ?: source?.name ?: "Unknown",
        date = publishedAt?.let { formatDate(it) } ?: "N/A",
        category = source?.name ?: "General",
        imageUrl = urlToImage
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.substringBefore("T") // Повертаємо хоча б дату, якщо парсинг не вдався
    }
}

