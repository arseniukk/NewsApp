package com.example.newsapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val category: String,
    val imageUrl: String? = null
)