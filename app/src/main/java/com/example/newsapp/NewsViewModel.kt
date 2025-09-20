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

/**
 * Клас, що представляє стан UI для головного екрану новин.
 * Він є єдиним джерелом правди для UI.
 * @param articles Поточний список статей для відображення (може бути відфільтрованим).
 * @param categories Список усіх доступних категорій для фільтрації.
 * @param selectedCategory Назва поточно обраної категорії.
 */
data class NewsUiState(
    val articles: List<Article> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Усі"
)

/**
 * ViewModel для екранів новин.
 * Відповідає за:
 * 1. Зберігання та управління станом UI (`NewsUiState`).
 * 2. Обробку дій користувача (вибір категорії, лайк статті).
 * 3. Надання даних для UI (список статей, конкретна стаття за ID).
 */
class NewsViewModel : ViewModel() {

    // Приватний, змінний StateFlow. Тільки ViewModel може змінювати його.
    private val _uiState = MutableStateFlow(NewsUiState())
    // Публічний, незмінний StateFlow. UI підписується на нього для отримання оновлень стану.
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    // SharedFlow для надсилання одноразових подій до UI, наприклад, для показу Snackbar.
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // У реальному додатку дані б завантажувалися з репозиторію (мережа, база даних).
    // Тут ми використовуємо статичний список для демонстрації.
    private val allArticles = sampleArticles

    init {
        // Завантажуємо початковий контент при створенні ViewModel.
        loadContent()
    }

    /**
     * Ініціалізує початковий стан UI: завантажує всі статті та визначає список категорій.
     */
    private fun loadContent() {
        // Створюємо список категорій, додаючи "Усі" на початок.
        val categories = listOf("Усі") + allArticles.map { it.category }.distinct()
        // Оновлюємо стан UI початковими даними.
        _uiState.value = NewsUiState(
            articles = allArticles,
            categories = categories,
            selectedCategory = "Усі"
        )
    }

    /**
     * Обробляє подію вибору категорії користувачем.
     * Фільтрує список статей та оновлює стан UI.
     * @param category Назва обраної категорії.
     */
    fun selectCategory(category: String) {
        // Фільтруємо список статей відповідно до обраної категорії.
        val filteredArticles = if (category == "Усі") {
            allArticles
        } else {
            allArticles.filter { it.category == category }
        }

        // Оновлюємо стан UI. `update` є потокобезпечним способом зміни стану.
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                articles = filteredArticles
            )
        }
    }

    /**
     * Обробляє подію лайку статті.
     * Надсилає одноразову подію для показу Snackbar.
     * @param article Стаття, яку лайкнули.
     */
    fun onArticleLiked(article: Article) {
        // Запускаємо корутину в скоупі ViewModel для виконання асинхронної операції.
        viewModelScope.launch {
            _snackbarEvent.emit("Вам сподобалася стаття: ${article.title}")
        }
    }

    /**
     * Повертає статтю за її унікальним ID.
     * Ця функція використовується на екрані деталей статті.
     * @param id Унікальний ідентифікатор статті.
     * @return Об'єкт [Article] або null, якщо статтю не знайдено.
     */
    fun getArticleById(id: Int): Article? {
        // Використовуємо функцію `find` для пошуку першого елемента, що відповідає умові.
        return allArticles.find { it.id == id }
    }
}