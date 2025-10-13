package com.example.newsapp

import com.example.newsapp.network.ArticleDto
import com.example.newsapp.network.NewsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale

class NewsRepository(private val articleDao: ArticleDao) {

    fun getArticles(category: String): Flow<List<Article>> {
        return articleDao.getArticlesByCategory(category).map { entities ->
            entities.map { entity ->
                entity.toArticle()
            }
        }
    }

    suspend fun refreshArticles(category: String) {
        try {
            val articlesFromApi = NewsApi.retrofitService.getTopHeadlines(category = category)
            val articleEntities = articlesFromApi.articles.mapNotNull { dto ->
                dto.toArticleEntity(category)
            }
            articleDao.clearArticlesByCategory(category)
            articleDao.insertArticles(articleEntities)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

// --- ВСІ ФУНКЦІЇ-МАППЕРИ МАЮТЬ БУТИ ТУТ ---

fun ArticleEntity.toArticle(): Article {
    return Article(
        id = this.id,
        title = this.title,
        description = this.description,
        author = this.author,
        date = this.date,
        category = this.category,
        imageUrl = this.imageUrl
    )
}

fun ArticleDto.toArticleEntity(category: String): ArticleEntity? {
    if (title.isNullOrEmpty() || description.isNullOrEmpty() || url.isNullOrEmpty()) {
        return null
    }
    return ArticleEntity(
        id = url.hashCode(),
        title = title,
        description = description,
        author = author ?: source?.name ?: "Unknown",
        date = publishedAt?.let { formatDate(it) } ?: "N/A",
        category = category,
        imageUrl = urlToImage
    )
}

// Функція для конвертації Article (з UI) в SavedArticleEntity (для збереження)
fun Article.toSavedArticleEntity() = SavedArticleEntity(
    id = id,
    title = title,
    description = description,
    author = author,
    date = date,
    category = category,
    imageUrl = imageUrl
)

// Функція для конвертації SavedArticleEntity (зі збережених) в Article (для UI)
fun SavedArticleEntity.toArticle() = Article(
    id = id,
    title = title,
    description = description,
    author = author,
    date = date,
    category = category,
    imageUrl = imageUrl
)

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.substringBefore("T")
    }
}