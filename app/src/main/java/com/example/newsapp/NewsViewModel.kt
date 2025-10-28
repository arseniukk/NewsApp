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

    private val articleDao = AppDatabase.getDatabase(application).articleDao()
    private val newsRepository = NewsRepository()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // --- Потоки з бази даних ---
    val savedArticles: StateFlow<List<SavedArticleEntity>> = articleDao.getAllSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedArticleIds: StateFlow<Set<Int>> = articleDao.getAllLikedArticleIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- Потоки для стрічки новин (пагінація) ---
    private val _selectedCategory = MutableStateFlow("general")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _newsSource = MutableStateFlow(NewsSource.NEWS_API)
    val newsSource: StateFlow<NewsSource> = _newsSource.asStateFlow()

    val articles: Flow<PagingData<Article>> = combine(
        _selectedCategory,
        _newsSource
    ) { category, source ->
        Pair(category, source)
    }.flatMapLatest { (category, source) ->
        newsRepository.getArticlesStream(source, category)
    }.cachedIn(viewModelScope)

    // --- Потік для екрану аналітики ---
    private val _categoryCounts = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val categoryCounts: StateFlow<List<Pair<String, Int>>> = _categoryCounts.asStateFlow()

    init {
        // Запускаємо корутину, яка буде слухати зміни в збережених статтях
        // і автоматично оновлювати дані для графіка.
        viewModelScope.launch {
            savedArticles.collect { savedList ->
                val counts = savedList
                    .groupBy { it.category } // Групуємо статті за категорією
                    .map { (category, articles) -> category to articles.size } // Рахуємо кількість у кожній групі
                    .sortedByDescending { it.second } // Сортуємо (найбільші стовпці будуть зліва)
                _categoryCounts.value = counts
            }
        }
    }

    // --- Функції, які викликає UI ---

    fun selectCategory(category: String) {
        _selectedCategory.value = category.lowercase()
    }

    fun selectNewsSource(source: NewsSource) {
        _newsSource.value = source
    }

    fun getArticleById(id: Int): Article? {
        // Пошук статті для екрану деталей
        val savedArticle = savedArticles.value.find { it.id == id }
        // Ми не можемо надійно шукати в PagingData, тому шукаємо хоча б у збережених.
        // Це обмеження, яке можна покращити, маючи повний кеш у БД.
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