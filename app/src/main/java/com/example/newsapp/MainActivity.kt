package com.example.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.* // Використовуємо Material3
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // Правильний імпорт для rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Використовуємо тему Material3
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewsApp()
                }
            }
        }
    }
}

// Головна Composable функція для додатку новин
@OptIn(ExperimentalMaterial3Api::class) // Позначаємо, що TopAppBar є експериментальним API
@Composable
fun NewsApp() {
    // Scaffold надає базову структуру для Material Design компонентів
    Scaffold(
        topBar = {
            TopAppBar( // Використовуємо TopAppBar з Material3
                title = { Text("Мої Новини") }
            )
        }
    ) { paddingValues ->
        NewsList(sampleArticles, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
fun NewsList(articles: List<Article>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(16.dp)) {
        items(articles) { article ->
            NewsItem(article)
            // Додаємо роздільник між статтями для кращої читабельності
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun NewsItem(article: Article) {
    // State та Recomposition: Використовуємо rememberSaveable для збереження стану при зміні конфігурації
    var likesCount by rememberSaveable { mutableStateOf(0) }

    // Column для вертикального розташування елементів статті
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp) // Модифікатор: відступ з усіх боків
    ) {
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineSmall, // Модифікатор: стиль тексту
            modifier = Modifier.padding(bottom = 4.dp) // Модифікатор: відступ знизу
        )
        Text(
            text = article.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Row для горизонтального розташування автора та дати
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // Модифікатор: розташування елементів
        ) {
            Text(
                text = "by ${article.author}",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = article.date,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp)) // Модифікатор: простір між елементами

        // Row для кнопки лайка та лічильника
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End // Модифікатор: вирівнювання праворуч
        ) {
            // Button: основний компонент UI
            Button(
                onClick = { likesCount++ }, // Збільшення лічильника лайків
                // Модифікатор: зменшуємо відступ для кнопки
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Filled.ThumbUp, contentDescription = "Like")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Like")
            }
            // Text: відображення лічильника лайків
            Text(
                text = "$likesCount",
                style = MaterialTheme.typography.bodyLarge,
                // Модифікатор: вирівнювання тексту
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}