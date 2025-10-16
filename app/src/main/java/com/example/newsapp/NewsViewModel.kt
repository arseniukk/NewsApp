package com.example.newsapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(application: Application) : AndroidViewModel(application) {

    // Репозиторій тепер не потребує DAO для цього завдання
    private val newsRepository = NewsRepository()
    private val articleDao = AppDatabase.getDatabase(application).articleDao()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // --- Потоки з бази даних для лайків та збережень (залишаються без змін) ---
    val savedArticles: StateFlow<List<SavedArticleEntity>> = articleDao.getAllSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedArticleIds: StateFlow<Set<Int>> = articleDao.getAllLikedArticleIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- РЕАКТИВНИЙ ПОТІК ДЛЯ ПАГІНАЦІЇ ---
    private val _selectedCategory = MutableStateFlow("general")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    /**
     * Основний потік даних для UI.
     * Він реагує на зміну категорії та створює новий потік PagingData.
     * .cachedIn(viewModelScope) є надзвичайно важливим: він кешує дані
     * і дозволяє їм пережити зміну конфігурації (наприклад, поворот екрану),
     * не завантажуючи все заново.
     */
    val articles: Flow<PagingData<Article>> = _selectedCategory
        .flatMapLatest { category ->
            newsRepository.getArticlesStream(category)
        }
        .cachedIn(viewModelScope)

    /**
     * Змінює поточну категорію.
     * UI автоматично оновить список, оскільки articles залежить від _selectedCategory.
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category.lowercase()
    }

    // --- Функції для лайків та збережень (залишаються без змін) ---

    fun getArticleById(id: Int): Article? {
        // Ця функція більше не може надійно працювати, оскільки ми не маємо повного списку
        // статей. Для детального екрану дані краще передавати напряму.
        // Поки що залишаємо як заглушку, що шукає лише у збережених.
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