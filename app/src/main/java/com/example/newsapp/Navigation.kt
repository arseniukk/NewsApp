package com.example.newsapp

// Аргумент, який ми будемо передавати - ID статті
const val ARTICLE_ID_ARG = "articleId"

/**
 * Sealed class для визначення навігаційних маршрутів.
 * Це запобігає помилкам, оскільки ми посилаємося на об'єкти, а не на рядки.
 */
sealed class Screen(val route: String) {
    // Маршрут для головного екрану
    object HomeScreen : Screen("home_screen")

    // Маршрут для екрану деталей статті. Він включає місце для аргументу.
    object ArticleDetailScreen : Screen("article_detail_screen/{$ARTICLE_ID_ARG}") {
        /**
         * Функція для створення повного маршруту з конкретним ID статті.
         * Замість того, щоб писати "article_detail_screen/123" вручну,
         * ми викликаємо Screen.ArticleDetailScreen.createRoute(123).
         */
        fun createRoute(articleId: Int) = "article_detail_screen/$articleId"
    }
}