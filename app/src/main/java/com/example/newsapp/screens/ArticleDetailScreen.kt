package com.example.newsapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newsapp.Article

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: Article,
    onNavigateUp: () -> Unit // Функція для повернення на попередній екран
) {
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Додаємо можливість прокрутки
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
                // Повторюємо опис, щоб продемонструвати скролінг
                text = article.description + "\n\n" + article.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}