package com.example.newsapp

data class Article(
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val imageUrl: String? = null
)
