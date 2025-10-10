package com.example.newsapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

// Новий sealed class для представлення стану завантаження даних.
// Він більш гнучкий, ніж просто isLoading та error.
sealed interface ArticlesUiState {
    data class Success(val articles: List<Article>) : ArticlesUiState
    data class Error(val message: String) : ArticlesUiState
    object Loading : ArticlesUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val articleDao = AppDatabase.getDatabase(application).articleDao()
    private val newsRepository = NewsRepository()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // --- Потоки з бази даних (залишаються без змін) ---
    val savedArticles: StateFlow<List<SavedArticleEntity>> = articleDao.getAllSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedArticleIds: StateFlow<Set<Int>> = articleDao.getAllLikedArticleIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- РЕАКТИВНА ЧАСТИНА ---

    // 1. Потік, що зберігає поточну обрану категорію.
    // Початкове значення "general" - перша категорія, яку ми завантажимо.
    private val _selectedCategory = MutableStateFlow("general")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 2. Основний потік, що завантажує статті.
    // flatMapLatest автоматично скасовує попередній запит і запускає новий,
    // коли значення _selectedCategory змінюється. Це і є суть реактивності.
    val articlesUiState: StateFlow<ArticlesUiState> = _selectedCategory
        .flatMapLatest { category ->
            flow {
                emit(ArticlesUiState.Loading) // Повідомляємо UI, що почалося завантаження
                try {
                    // Викликаємо репозиторій з поточною категорією
                    val articles = newsRepository.getTopHeadlines(category)
                    emit(ArticlesUiState.Success(articles)) // Надсилаємо успішний результат
                } catch (e: IOException) {
                    emit(ArticlesUiState.Error("Помилка мережі. Перевірте з'єднання з інтернетом."))
                } catch (e: Exception) {
                    emit(ArticlesUiState.Error("Не вдалося завантажити новини: ${e.message}"))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Потік активний, поки є хоч один підписник
            initialValue = ArticlesUiState.Loading // Початковий стан - завантаження, щоб UI одразу показав індикатор
        )

    // Функція для зміни категорії, яку викликає UI
    fun selectCategory(category: String) {
        _selectedCategory.value = category.lowercase() // Зберігаємо в нижньому регістрі для API
    }

    // --- Інші функції ---

    fun getArticleById(id: Int): Article? {
        // Шукаємо статтю в уже завантаженому списку...
        val articleFromApi = (articlesUiState.value as? ArticlesUiState.Success)?.articles?.find { it.id == id }
        if (articleFromApi != null) {
            return articleFromApi
        }
        // ...або в списку збережених, якщо перейшли з екрану "Збережене"
        val savedArticle = savedArticles.value.find { it.id == id }
        return savedArticle?.toArticle()
    }

    fun toggleSaveArticle(article: Article) {
        viewModelScope.launch {
            val isCurrentlySaved = articleDao.isArticleSaved(article.id).first()
            if (isCurrentlySaved) {
                articleDao.deleteSavedArticle(article.toSavedArticleEntity())
                _snackbarEvent.emit("Статтю видалено зі збережених")
            } else {
                articleDao.insertSavedArticle(article.toSavedArticleEntity())
                _snackbarEvent.emit("Статтю збережено")
            }
        }
    }

    fun isArticleSaved(articleId: Int): StateFlow<Boolean> {
        return articleDao.isArticleSaved(articleId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    }

    fun toggleLikeArticle(article: Article) {
        viewModelScope.launch {
            if (likedArticleIds.value.contains(article.id)) {
                articleDao.deleteLikedArticleId(article.id)
            } else {
                articleDao.insertLikedArticleId(LikedArticleId(id = article.id))
            }
        }
    }
}

// Допоміжні функції-маппери
fun Article.toSavedArticleEntity() = SavedArticleEntity(
    id = id, title = title, description = description, author = author,
    date = date, category = category, imageUrl = imageUrl
)

fun SavedArticleEntity.toArticle() = Article(
    id = id, title = title, description = description, author = author,
    date = date, category = category, imageUrl = imageUrl
)