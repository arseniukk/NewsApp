package com.example.newsapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable // +++ Додано імпорт

@Entity(tableName = "saved_articles")
@Serializable // +++ Додано анотацію для автоматичної конвертації в JSON
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