package com.example.newsapp

const val ARTICLE_ID_ARG = "articleId"

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
    object SavedScreen : Screen("saved_screen")
    object ArticleDetailScreen : Screen("article_detail_screen/{$ARTICLE_ID_ARG}") {
        fun createRoute(articleId: Int) = "article_detail_screen/$articleId"
    }
}