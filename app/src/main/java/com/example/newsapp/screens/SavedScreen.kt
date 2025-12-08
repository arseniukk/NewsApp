package com.example.newsapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.newsapp.Article
import com.example.newsapp.BiometricAuthenticator
import com.example.newsapp.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: NewsViewModel,
    onArticleClick: (Int) -> Unit
) {
    val savedArticles by viewModel.savedArticles.collectAsState()
    val likedArticleIds by viewModel.likedArticleIds.collectAsState()

    // --- СТАН БЛОКУВАННЯ ---
    var isAuthenticated by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val biometricAuthenticator = remember { BiometricAuthenticator(context) }

    // Функція для запуску біометрії
    fun authenticate() {
        if (biometricAuthenticator.canAuthenticate()) {
            biometricAuthenticator.promptBiometricAuth(
                title = "Доступ до збережених",
                subTitle = "Підтвердіть особу для перегляду",
                negativeButtonText = "Скасувати",
                onSuccess = {
                    isAuthenticated = true
                },
                onError = { _, errorString ->
                    Toast.makeText(context, "Помилка: $errorString", Toast.LENGTH_SHORT).show()
                },
                onFailed = {
                    Toast.makeText(context, "Не розпізнано", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Якщо біометрія недоступна, просто пускаємо (або показуємо помилку)
            isAuthenticated = true
            Toast.makeText(context, "Біометрія недоступна", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Збережені статті") })
        }
    ) { paddingValues ->

        // --- ПЕРЕВІРКА СТАНУ ---
        if (!isAuthenticated) {
            // ЕКРАН БЛОКУВАННЯ
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text("Цей розділ захищено", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { authenticate() }) {
                    Text("Розблокувати")
                }
            }
        } else {
            // КОНТЕНТ (ЯКЩО РОЗБЛОКОВАНО)
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
                            isSaved = true,
                            onItemClick = { onArticleClick(article.id) },
                            onLikeClick = { viewModel.toggleLikeArticle(article) },
                            onSaveClick = { viewModel.toggleSaveArticle(article) }
                        )
                    }
                }
            }
        }
    }
}