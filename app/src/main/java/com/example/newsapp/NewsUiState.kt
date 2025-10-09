package com.example.newsapp

data class NewsUiState(
    val articles: List<Article> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Усі",
    val isLoading: Boolean = false,
    val error: String? = null
)