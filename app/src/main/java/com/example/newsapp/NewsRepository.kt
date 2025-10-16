package com.example.newsapp

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.newsapp.network.ArticleDto
import com.example.newsapp.network.NewsApi
import com.example.newsapp.network.NewsPagingSource
import com.example.newsapp.network.RssApi
import com.example.newsapp.rss.RssItem
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

enum class NewsSource {
    NEWS_API,
    UKRAINIAN_NEWS_RSS
}

class NewsRepository {
    fun getArticlesStream(source: NewsSource, category: String): Flow<PagingData<Article>> {
        return when (source) {
            NewsSource.NEWS_API -> {
                Pager(
                    config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                    pagingSourceFactory = { NewsPagingSource(NewsApi.retrofitService, category) }
                ).flow
            }
            NewsSource.UKRAINIAN_NEWS_RSS -> {
                Pager(
                    config = PagingConfig(pageSize = 100, enablePlaceholders = false),
                    pagingSourceFactory = { RssPagingSource() }
                ).flow
            }
        }
    }
}

class RssPagingSource : androidx.paging.PagingSource<Int, Article>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val response = RssApi.retrofitService.getUkrainianNews()
            val articles = response.channel?.items?.mapNotNull { it.toArticle() } ?: emptyList()
            LoadResult.Page(data = articles, prevKey = null, nextKey = null)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    override fun getRefreshKey(state: androidx.paging.PagingState<Int, Article>): Int? = null
}

// --- ФУНКЦІЇ-МАППЕРИ ---

fun ArticleDto.toArticle(): Article? {
    if (title.isNullOrEmpty() || description.isNullOrEmpty() || url.isNullOrEmpty()) return null
    return Article(
        id = url.hashCode(),
        title = title,
        description = description,
        author = author ?: source?.name ?: "Unknown",
        date = publishedAt?.let { formatDate(it) } ?: "N/A",
        category = source?.name ?: "General",
        imageUrl = urlToImage
    )
}

fun RssItem.toArticle(): Article? {
    if (title.isNullOrEmpty() || link.isNullOrEmpty()) return null
    val cleanDescription = description?.replace(Regex("<.*?>|&.*?;"), "") ?: ""
    return Article(
        id = link.hashCode(),
        title = title!!,
        description = cleanDescription,
        author = author ?: "Українська правда",
        date = pubDate?.let { formatDateRss(it) } ?: "N/A",
        category = "Україна",
        imageUrl = null
    )
}

fun Article.toSavedArticleEntity() = SavedArticleEntity(
    id = id, title = title, description = description, author = author,
    date = date, category = category, imageUrl = imageUrl
)

fun SavedArticleEntity.toArticle() = Article(
    id = id, title = title, description = description, author = author,
    date = date, category = category, imageUrl = imageUrl
)

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
    if (title.isNullOrEmpty() || description.isNullOrEmpty() || url.isNullOrEmpty()) return null
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

private fun formatDate(dateString: String): String {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(dateString)?.let {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it)
        } ?: dateString
    } catch (e: Exception) {
        dateString.substringBefore("T")
    }
}

private fun formatDateRss(dateString: String): String {
    return try {
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(dateString)?.let {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it)
        } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}