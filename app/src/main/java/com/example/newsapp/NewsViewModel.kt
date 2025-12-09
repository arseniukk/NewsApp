package com.example.newsapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val articleDao = AppDatabase.getDatabase(application).articleDao()
    private val newsRepository = NewsRepository()

    // Загальний канал для повідомлень (Snackbar)
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // --- MQTT СТАТУС (IoT) ---
    private val _mqttStatus = MutableSharedFlow<String>()
    val mqttStatus: SharedFlow<String> = _mqttStatus.asSharedFlow()

    // --- Тимчасовий кеш для статей, завантажених через пагінацію ---
    private val articlesCache = mutableMapOf<Int, Article>()

    // --- JSON Форматер для експорту ---
    private val jsonFormatter = Json { prettyPrint = true }

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
    }.map { pagingData ->
        // Коли Paging завантажує дані, ми зберігаємо їх у наш кеш для швидкого доступу
        pagingData.map { article ->
            articlesCache[article.id] = article
            article
        }
    }.cachedIn(viewModelScope)

    // --- Потік для екрану аналітики ---
    private val _categoryCounts = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val categoryCounts: StateFlow<List<Pair<String, Int>>> = _categoryCounts.asStateFlow()

    // --- Потік для реалтайм-даних з WebSocket ---
    private val _livePrice = MutableStateFlow<String?>(null)
    val livePrice: StateFlow<String?> = _livePrice.asStateFlow()

    private var priceJob: Job? = null

    init {
        // Запускаємо розрахунок статистики для графіка
        viewModelScope.launch {
            savedArticles.collect { savedList ->
                val counts = savedList
                    .groupBy { it.category }
                    .map { (category, articles) -> category to articles.size }
                    .sortedByDescending { it.second }
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

    /**
     * Знаходить статтю за ID.
     * Спочатку шукає в тимчасовому кеші, потім у списку збережених.
     */
    fun getArticleById(id: Int): Article? {
        val cachedArticle = articlesCache[id]
        if (cachedArticle != null) {
            return cachedArticle
        }
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

    // --- Функція для експорту даних (Завдання 26) ---
    fun getSavedArticlesJson(): String {
        val articles = savedArticles.value
        return if (articles.isNotEmpty()) {
            jsonFormatter.encodeToString(articles)
        } else {
            "[]" // Повертаємо порожній масив JSON, якщо немає статей
        }
    }

    // --- Функції для управління WebSocket (Завдання 16) ---

    fun startPriceMonitoring() {
        if (priceJob?.isActive == true) return
        priceJob = viewModelScope.launch {
            newsRepository.getPriceUpdates().collect { price ->
                _livePrice.value = price
            }
        }
    }

    fun stopPriceMonitoring() {
        priceJob?.cancel()
        newsRepository.stopPriceUpdates()
        _livePrice.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPriceMonitoring()
    }

    // --- Функції для MQTT (IoT - Завдання 20) ---

    fun sendSmartHomeAlert() {
        viewModelScope.launch {
            _mqttStatus.emit("Sending alert...")
            // Викликаємо наш MqttManager
            val success = MqttManager.connectAndPublish("ALERT: Breaking News Received! Color: RED")
            if (success) {
                _mqttStatus.emit("Сигнал тривоги відправлено на лампу!")
            } else {
                _mqttStatus.emit("Помилка підключення до розумного будинку")
            }
        }
    }
}