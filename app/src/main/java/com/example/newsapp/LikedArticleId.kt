package com.example.newsapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_articles")
data class LikedArticleId(
    @PrimaryKey
    val id: Int
)