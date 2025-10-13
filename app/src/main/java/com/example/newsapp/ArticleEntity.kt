package com.example.newsapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles_cache")
data class ArticleEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val category: String, // Категорія, за якою завантажили
    val imageUrl: String? = null
)