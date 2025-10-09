package com.example.newsapp

// Переконайтеся, що в цьому файлі є ТІЛЬКИ ЦЕЙ КЛАС

data class NewsUiState(
    val articles: List<Article> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Усі"
)