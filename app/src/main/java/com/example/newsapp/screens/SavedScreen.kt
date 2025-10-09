package com.example.newsapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.newsapp.Article // +++ ОСЬ ЦЕЙ ВАЖЛИВИЙ ІМПОРТ
import com.example.newsapp.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: NewsViewModel,
    onArticleClick: (Int) -> Unit
) {
    val savedArticles by viewModel.savedArticles.collectAsState()
    val likedArticleIds by viewModel.likedArticleIds.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Збережені статті") })
        }
    ) { paddingValues ->
        if (savedArticles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("У вас ще немає збережених статей", textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(savedArticles, key = { it.id }) { savedArticleEntity ->
                    // Конвертуємо SavedArticleEntity назад у Article для сумісності з NewsItem
                    val article = Article(
                        id = savedArticleEntity.id,
                        title = savedArticleEntity.title,
                        description = savedArticleEntity.description,
                        author = savedArticleEntity.author,
                        date = savedArticleEntity.date,
                        category = savedArticleEntity.category,
                        imageUrl = savedArticleEntity.imageUrl
                    )
                    NewsItem(
                        article = article,
                        isLiked = article.id in likedArticleIds,
                        isSaved = true, // На цьому екрані всі статті збережені
                        onItemClick = { onArticleClick(article.id) },
                        onLikeClick = { viewModel.toggleLikeArticle(article) },
                        onSaveClick = { viewModel.toggleSaveArticle(article) } // Дозволяємо видалити
                    )
                }
            }
        }
    }
}