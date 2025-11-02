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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsapp.Article
import com.example.newsapp.LocationUtils
import com.example.newsapp.NewsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: Article,
    viewModel: NewsViewModel,
    onNavigateUp: () -> Unit
) {
    val isSaved by viewModel.isArticleSaved(article.id).collectAsState()
    val likedIds by viewModel.likedArticleIds.collectAsState()
    val isLiked = article.id in likedIds

    val context = LocalContext.current
    var locationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Викликаємо Geocoding при першому відкритті екрану або при зміні статті
    LaunchedEffect(key1 = article.id) {
        // Запускаємо пошук координат у фоновій корутині
        coroutineScope.launch {
            val result = LocationUtils.getLatLngFromArticle(context, article)
            if (result != null) {
                locationLatLng = result
                // Зберігаємо назву знайденого місця, щоб показати на маркері
                locationName = LocationUtils.findFirstLocationInText(article)
            }
        }
    }

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
                actions = {
                    IconButton(onClick = { viewModel.toggleLikeArticle(article) }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Лайк"
                        )
                    }
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
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = "Зображення новини",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
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
                    text = article.description + "\n\n" + (article.description.takeIf { it.length > 10 } ?: ""),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // --- СЕКЦІЯ З КАРТОЮ ---
            // Показуємо карту, тільки якщо координати були успішно знайдені
            if (locationLatLng != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Місце події на карті",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(locationLatLng!!, 5f)
                }

                // Анімовано оновлюємо позицію камери, якщо координати змінилися
                LaunchedEffect(locationLatLng) {
                    locationLatLng?.let {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 5f))
                    }
                }

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(horizontal = 16.dp),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = locationLatLng!!),
                        title = locationName ?: "Місце події",
                        snippet = article.title
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}