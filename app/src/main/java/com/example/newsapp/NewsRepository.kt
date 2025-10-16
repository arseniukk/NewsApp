package com.example.newsapp

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.newsapp.network.ArticleDto
import com.example.newsapp.network.NewsApi
import com.example.newsapp.network.NewsPagingSource
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Репозиторій, що відповідає за надання даних для ViewModel.
 * Для завдання 11 він створює потік даних з пагінацією напряму з мережі.
 */
class NewsRepository {

    /**
     * Створює потік PagingData, який буде завантажувати статті посторінково.
     * @param category Категорія новин для завантаження.
     * @return Flow<PagingData<Article>>
     */
    fun getArticlesStream(category: String): Flow<PagingData<Article>> {
        return Pager(
            // Конфігурація пагінації: скільки елементів завантажувати за раз.
            config = PagingConfig(
                pageSize = 20, // Кількість елементів на сторінці
                enablePlaceholders = false
            ),
            // PagingSourceFactory - "фабрика", яка створює новий NewsPagingSource
            // для кожного нового запиту (наприклад, при зміні категорії).
            pagingSourceFactory = { NewsPagingSource(NewsApi.retrofitService, category) }
        ).flow // .flow перетворює Pager на Flow<PagingData<Article>>
    }
}

// --- ФУНКЦІЇ-МАППЕРИ ЗАЛИШАЮТЬСЯ ВАЖЛИВИМИ ---

/**
 * Конвертує [ArticleDto] (об'єкт з мережі) у [Article] (об'єкт для UI).
 * Використовується всередині NewsPagingSource.
 */
fun ArticleDto.toArticle(): Article? {
    // Відкидаємо статті без ключових полів, щоб уникнути крешів та порожніх елементів
    if (title.isNullOrEmpty() || description.isNullOrEmpty() || url.isNullOrEmpty()) {
        return null
    }
    return Article(
        id = url.hashCode(), // Генеруємо унікальний ID з URL
        title = title,
        description = description,
        author = author ?: source?.name ?: "Unknown",
        date = publishedAt?.let { formatDate(it) } ?: "N/A",
        // Категорію тепер беремо з джерела, якщо воно є
        category = source?.name ?: "General",
        imageUrl = urlToImage
    )
}

/**
 * Конвертує [Article] (з UI) у [SavedArticleEntity] (для збереження в базу даних).
 */
fun Article.toSavedArticleEntity() = SavedArticleEntity(
    id = id,
    title = title,
    description = description,
    author = author,
    date = date,
    category = category,
    imageUrl = imageUrl
)

/**
 * Конвертує [SavedArticleEntity] (зі збережених) у [Article] (для UI).
 */
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