package com.example.newsapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// NewsList тепер приймає список статей і обробник події лайку.
@Composable
fun NewsList(
    articles: List<Article>,
    onArticleLiked: (Article) -> Unit, // State Hoisting: передаємо подію нагору
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        items(articles, key = { it.id }) { article -> // Використовуємо `key` для оптимізації
            NewsItem(
                article = article,
                onLikeClicked = { onArticleLiked(article) } // Передаємо подію далі
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

// NewsItem тепер повністю stateless (без стану). Він не має власного `remember`.
// Він просто відображає дані та повідомляє про дії.
@Composable
fun NewsItem(
    article: Article,
    onLikeClicked: () -> Unit // State Hoisting: приймає лямбду для обробки кліку
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onLikeClicked, // Викликаємо передану функцію
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = "Like")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Like")
                }
                // Ми видалили лічильник лайків, оскільки тепер використовуємо Snackbar
            }
        }
    }
}