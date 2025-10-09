package com.example.newsapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// У вас цей клас має бути в окремому файлі NewsUiState.kt
// data class NewsUiState(...)

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val articleDao = AppDatabase.getDatabase(application).articleDao()

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val allArticles = sampleArticles

    // --- Потоки даних з бази даних ---
    val savedArticles: StateFlow<List<SavedArticleEntity>> = articleDao.getAllSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedArticleIds: StateFlow<Set<Int>> = articleDao.getAllLikedArticleIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())


    init {
        loadContent()
    }

    private fun loadContent() {
        val categories = listOf("Усі") + allArticles.map { it.category }.distinct()
        _uiState.value = NewsUiState(
            articles = allArticles,
            categories = categories,
            selectedCategory = "Усі"
        )
    }

    // +++ ПОВЕРТАЄМО ФУНКЦІЮ ДЛЯ ФІЛЬТРАЦІЇ +++
    fun selectCategory(category: String) {
        val filteredArticles = if (category == "Усі") {
            allArticles
        } else {
            allArticles.filter { it.category == category }
        }

        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                articles = filteredArticles
            )
        }
    }

    fun getArticleById(id: Int): Article? {
        // У реальному додатку з API, тут був би запит до репозиторію
        // або пошук у списку вже завантажених статей.
        return allArticles.find { it.id == id }
    }

    // --- Логіка для збереження (Bookmarks) ---
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

    // --- Логіка для лайків (Likes) ---
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

// Допоміжна функція для конвертації
fun Article.toSavedArticleEntity() = SavedArticleEntity(
    id = id,
    title = title,
    description = description,
    author = author,
    date = date,
    category = category,
    imageUrl = imageUrl
)