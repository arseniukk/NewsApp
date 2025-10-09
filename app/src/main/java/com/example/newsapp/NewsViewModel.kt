package com.example.newsapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException



class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val articleDao = AppDatabase.getDatabase(application).articleDao()
    private val newsRepository = NewsRepository()

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    val savedArticles: StateFlow<List<SavedArticleEntity>> = articleDao.getAllSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedArticleIds: StateFlow<Set<Int>> = articleDao.getAllLikedArticleIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        loadTopHeadlines()
    }

    fun loadTopHeadlines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val articlesFromApi = newsRepository.getTopHeadlines()
                _uiState.update {
                    it.copy(isLoading = false, articles = articlesFromApi)
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Помилка мережі. Перевірте з'єднання з інтернетом.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Не вдалося завантажити новини: ${e.message}") }
            }
        }
    }

    fun getArticleById(id: Int): Article? {
        // Спочатку шукаємо статтю серед завантажених з мережі
        val articleFromApi = uiState.value.articles.find { it.id == id }
        if (articleFromApi != null) {
            return articleFromApi
        }
        // Якщо не знайшли (наприклад, перейшли зі збережених), шукаємо її там
        val savedArticle = savedArticles.value.find { it.id == id }
        return savedArticle?.let {
            Article(
                id = it.id,
                title = it.title,
                description = it.description,
                author = it.author,
                date = it.date,
                category = it.category,
                imageUrl = it.imageUrl
            )
        }
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

fun Article.toSavedArticleEntity() = SavedArticleEntity(
    id = id,
    title = title,
    description = description,
    author = author,
    date = date,
    category = category,
    imageUrl = imageUrl
)