package com.example.newsapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newsapp.Article
import com.example.newsapp.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: Article,
    viewModel: NewsViewModel,
    onNavigateUp: () -> Unit
) {
    // Підписуємося на стан: чи збережена ця стаття
    val isSaved by viewModel.isArticleSaved(article.id).collectAsState()

    // Підписуємося на стан: чи лайкнута ця стаття
    val likedIds by viewModel.likedArticleIds.collectAsState()
    val isLiked = article.id in likedIds

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.category, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                // Панель дій з кнопками "Лайк" та "Зберегти"
                actions = {
                    // Іконка-кнопка для лайку
                    IconButton(onClick = { viewModel.toggleLikeArticle(article) }) {
                        Icon(
                            // Використовуємо різні іконки для стану "лайкнуто"
                            imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Лайк"
                        )
                    }

                    // Іконка-кнопка для збереження
                    IconButton(onClick = { viewModel.toggleSaveArticle(article) }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Зберегти"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(article.title, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(article.author, style = MaterialTheme.typography.labelMedium)
                Text(article.date, style = MaterialTheme.typography.labelMedium)
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Text(
                text = article.description + "\n\n" + article.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}