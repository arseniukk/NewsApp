package com.example.newsapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val articleDao = AppDatabase.getDatabase(application).articleDao()
    private val newsRepository = NewsRepository(articleDao)

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // --- Потоки даних з бази даних ---
    val savedArticles: StateFlow<List<SavedArticleEntity>> = articleDao.getAllSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedArticleIds: StateFlow<Set<Int>> = articleDao.getAllLikedArticleIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- Реактивний потік для стрічки новин ---
    private val _selectedCategory = MutableStateFlow("general")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // articles тепер є потоком з репозиторію, який читає з кешу.
    val articles: StateFlow<List<Article>> = _selectedCategory
        .flatMapLatest { category ->
            newsRepository.getArticles(category)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // При першому запуску додатку одразу оновлюємо кеш для початкової категорії
        refreshCurrentCategory()
    }

    /**
     * Запускає примусове оновлення кешу для поточної обраної категорії.
     * Можна викликати, наприклад, при pull-to-refresh.
     */
    fun refreshCurrentCategory() {
        viewModelScope.launch {
            try {
                newsRepository.refreshArticles(_selectedCategory.value)
            } catch (e: Exception) {
                _snackbarEvent.emit("Не вдалося оновити новини")
            }
        }
    }

    /**
     * Змінює поточну категорію та запускає оновлення кешу для неї.
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category.lowercase()
        // Одразу запускаємо оновлення. UI автоматично підхопить зміни з бази даних.
        refreshCurrentCategory()
    }

    /**
     * Знаходить статтю за ID.
     * Спочатку шукає серед завантажених у кеші, потім серед збережених.
     */
    fun getArticleById(id: Int): Article? {
        val articleFromCache = articles.value.find { it.id == id }
        if (articleFromCache != null) {
            return articleFromCache
        }
        val savedArticle = savedArticles.value.find { it.id == id }
        return savedArticle?.toArticle()
    }

    /**
     * Зберігає або видаляє статтю зі "Збережених".
     */
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

    /**
     * Повертає потік, що показує, чи збережена стаття.
     */
    fun isArticleSaved(articleId: Int): StateFlow<Boolean> {
        return articleDao.isArticleSaved(articleId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    }

    /**
     * Додає або видаляє "лайк" для статті.
     */
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