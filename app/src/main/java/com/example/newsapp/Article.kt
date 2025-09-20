package com.example.newsapp

data class Article(
    val id: Int, // Додано ID для унікальності
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val category: String, // Додано поле категорії
    val imageUrl: String? = null
)