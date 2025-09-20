package com.example.newsapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Клас, що представляє стан UI. Він містить всі дані, які потрібні для відображення екрану.
data class NewsUiState(
    val articles: List<Article> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Усі"
)

class NewsViewModel : ViewModel() {

    // Приватний MutableStateFlow, який може змінювати тільки ViewModel.
    private val _uiState = MutableStateFlow(NewsUiState())
    // Публічний, незмінний StateFlow, на який підписується UI.
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    // SharedFlow для одноразових подій, як-от показ Snackbar.
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val allArticles = sampleArticles // У реальному додатку дані б завантажувалися з мережі або бази даних.

    init {
        // Ініціалізація початкового стану при створенні ViewModel.
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

    // Цей метод викликається з UI, коли користувач обирає категорію.
    fun selectCategory(category: String) {
        val filteredArticles = if (category == "Усі") {
            allArticles
        } else {
            allArticles.filter { it.category == category }
        }

        // Оновлюємо стан за допомогою функції update. Це безпечно для потоків.
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                articles = filteredArticles
            )
        }
    }

    // Цей метод викликається з UI, коли користувач лайкає статтю.
    fun onArticleLiked(article: Article) {
        // У реальному додатку тут можна було б оновити лічильник лайків у базі даних.
        // Ми ж просто покажемо Snackbar.
        viewModelScope.launch {
            _snackbarEvent.emit("Вам сподобалася стаття: ${article.title}")
        }
    }
}