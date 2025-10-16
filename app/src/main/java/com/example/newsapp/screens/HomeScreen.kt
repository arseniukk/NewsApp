package com.example.newsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.newsapp.Article
import com.example.newsapp.NewsSource
import com.example.newsapp.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NewsViewModel,
    onArticleClick: (Int) -> Unit
) {
    val lazyPagingItems = viewModel.articles.collectAsLazyPagingItems()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val newsSource by viewModel.newsSource.collectAsState()
    val savedArticles by viewModel.savedArticles.collectAsState()
    val savedArticleIds = savedArticles.map { it.id }.toSet()
    val likedArticleIds by viewModel.likedArticleIds.collectAsState()
    val categories = listOf("General", "Business", "Technology", "Sports", "Science")

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Мої Новини") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            SourceSelector(
                selectedSource = newsSource,
                onSourceSelected = { viewModel.selectNewsSource(it) }
            )
            if (newsSource == NewsSource.NEWS_API) {
                CategoriesRow(
                    categories = categories,
                    selectedCategory = selectedCategory.replaceFirstChar { it.uppercase() },
                    onCategorySelected = { category -> viewModel.selectCategory(category) }
                )
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = { index -> lazyPagingItems.peek(index)?.id ?: index }
                ) { index ->
                    lazyPagingItems[index]?.let { article ->
                        NewsItem(
                            article = article,
                            isLiked = article.id in likedArticleIds,
                            isSaved = article.id in savedArticleIds,
                            onItemClick = { onArticleClick(article.id) },
                            onLikeClick = { viewModel.toggleLikeArticle(article) },
                            onSaveClick = { viewModel.toggleSaveArticle(article) }
                        )
                    }
                }
                lazyPagingItems.loadState.apply {
                    when {
                        refresh is LoadState.Loading -> {
                            item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                        }
                        append is LoadState.Loading -> {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                        }
                        refresh is LoadState.Error -> {
                            val e = refresh as LoadState.Error
                            item {
                                Column(modifier = Modifier.fillParentMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Помилка: ${e.error.localizedMessage}", color = MaterialTheme.colorScheme.error)
                                    Button(onClick = { lazyPagingItems.retry() }) { Text("Спробувати ще") }
                                }
                            }
                        }
                        append is LoadState.Error -> {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { Button(onClick = { lazyPagingItems.retry() }) { Text("Повторити") } } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourceSelector(
    selectedSource: NewsSource,
    onSourceSelected: (NewsSource) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FilterChip(
            selected = selectedSource == NewsSource.NEWS_API,
            onClick = { onSourceSelected(NewsSource.NEWS_API) },
            label = { Text("Світові новини") }
        )
        Spacer(Modifier.width(8.dp))
        FilterChip(
            selected = selectedSource == NewsSource.UKRAINIAN_NEWS_RSS,
            onClick = { onSourceSelected(NewsSource.UKRAINIAN_NEWS_RSS) },
            label = { Text("Новини України (RSS)") }
        )
    }
}

@Composable
fun CategoriesRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category.equals(selectedCategory, ignoreCase = true),
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
fun NewsItem(
    article: Article,
    isLiked: Boolean,
    isSaved: Boolean,
    onItemClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // --- ОСЬ ВИПРАВЛЕННЯ ---
            if (article.imageUrl != null) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = "Зображення новини",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Якщо URL зображення немає, показуємо заглушку
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            // --- КІНЕЦЬ ВИПРАВЛЕННЯ ---

            Column(modifier = Modifier.padding(16.dp)) {
                Text(article.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "by ${article.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = article.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSaveClick) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Зберегти",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onLikeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isLiked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = "Like")
                        Spacer(Modifier.width(8.dp))
                        Text("Like")
                    }
                }
            }
        }
    }
}