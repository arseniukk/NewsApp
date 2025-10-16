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

    private val newsRepository = NewsRepository()
    private val articleDao = AppDatabase.getDatabase(application).articleDao()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // --- Потоки з бази даних для лайків та збережень ---
    val savedArticles: StateFlow<List<SavedArticleEntity>> = articleDao.getAllSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedArticleIds: StateFlow<Set<Int>> = articleDao.getAllLikedArticleIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- РЕАКТИВНИЙ ПОТІК ДЛЯ ПАГІНАЦІЇ, ЩО ЗАЛЕЖИТЬ ВІД ДВОХ ПАРАМЕТРІВ ---
    private val _selectedCategory = MutableStateFlow("general")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _newsSource = MutableStateFlow(NewsSource.NEWS_API)
    val newsSource: StateFlow<NewsSource> = _newsSource.asStateFlow()

    /**
     * Основний потік даних для UI.
     * Використовуємо `combine` для об'єднання двох потоків: обраної категорії та обраного джерела.
     * `flatMapLatest` реагує на будь-яку зміну в цій парі та запускає новий запит до репозиторію.
     */
    val articles: Flow<PagingData<Article>> = combine(
        _selectedCategory,
        _newsSource
    ) { category, source ->
        Pair(category, source)
    }.flatMapLatest { (category, source) ->
        newsRepository.getArticlesStream(source, category)
    }.cachedIn(viewModelScope)


    /**
     * Змінює поточну категорію.
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category.lowercase()
    }

    /**
     * Змінює поточне джерело новин.
     */
    fun selectNewsSource(source: NewsSource) {
        _newsSource.value = source
    }


    // --- Функції для лайків та збережень (залишаються без змін) ---

    fun getArticleById(id: Int): Article? {
        // Ця логіка залишається складною з пагінацією.
        // Для простоти, шукаємо тільки у збережених.
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