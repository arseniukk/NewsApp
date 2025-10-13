package com.example.newsapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    // --- Методи для кешу новин (Offline-first) ---

    /**
     * Вставляє список статей у таблицю кешу.
     * Якщо стаття з таким ID вже існує, вона буде замінена.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    /**
     * Видаляє всі статті з кешу для певної категорії.
     * Це потрібно, щоб очистити старі дані перед вставкою нових.
     */
    @Query("DELETE FROM articles_cache WHERE category = :category")
    suspend fun clearArticlesByCategory(category: String)

    /**
     * Повертає потік (Flow) зі списком статей для певної категорії з кешу.
     * UI буде автоматично оновлюватися при зміні даних у цій таблиці.
     */
    @Query("SELECT * FROM articles_cache WHERE category = :category ORDER BY date DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>


    // --- Методи для збережених статей (Bookmarks) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedArticle(article: SavedArticleEntity)

    @Delete
    suspend fun deleteSavedArticle(article: SavedArticleEntity)

    @Query("SELECT * FROM saved_articles ORDER BY date DESC")
    fun getAllSavedArticles(): Flow<List<SavedArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE id = :articleId)")
    fun isArticleSaved(articleId: Int): Flow<Boolean>


    // --- Методи для лайкнутих статей (Likes) ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLikedArticleId(likedArticleId: LikedArticleId)

    @Query("DELETE FROM liked_articles WHERE id = :articleId")
    suspend fun deleteLikedArticleId(articleId: Int)

    @Query("SELECT id FROM liked_articles")
    fun getAllLikedArticleIds(): Flow<List<Int>>
}