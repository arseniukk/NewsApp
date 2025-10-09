package com.example.newsapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    // --- Операції для збережених статей (Saved) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedArticle(article: SavedArticleEntity)

    @Delete
    suspend fun deleteSavedArticle(article: SavedArticleEntity)

    @Query("SELECT * FROM saved_articles ORDER BY date DESC")
    fun getAllSavedArticles(): Flow<List<SavedArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE id = :articleId)")
    fun isArticleSaved(articleId: Int): Flow<Boolean>

    // --- Операції для лайкнутих статей (Likes) ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLikedArticleId(likedArticleId: LikedArticleId)

    @Query("DELETE FROM liked_articles WHERE id = :articleId")
    suspend fun deleteLikedArticleId(articleId: Int)

    @Query("SELECT id FROM liked_articles")
    fun getAllLikedArticleIds(): Flow<List<Int>>
}