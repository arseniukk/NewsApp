package com.example.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newsapp.ui.theme.NewsAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewsAppMainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsAppMainScreen() {
    val navController = rememberNavController()
    val newsViewModel: NewsViewModel = viewModel() // Створення ViewModel
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Підписка на SharedFlow для показу Snackbar
    LaunchedEffect(Unit) {
        newsViewModel.snackbarEvent.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Головна", "Категорії", "Збережене")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Мої Новини") })
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (item) {
                                "Головна" -> Icon(Icons.Filled.Home, contentDescription = item)
                                "Категорії" -> Icon(Icons.Filled.Category, contentDescription = item)
                                "Збережене" -> Icon(Icons.Filled.Bookmark, contentDescription = item)
                            }
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (item) {
                                "Головна" -> navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    restoreState = true
                                }
                                "Категорії" -> navController.navigate("categories") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    restoreState = true
                                }
                                "Збережене" -> navController.navigate("saved") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) } // Додано хост для Snackbar
    ) { paddingValues ->
        NavHost(navController, startDestination = "home", modifier = Modifier.padding(paddingValues)) {
            composable("home") {
                HomeScreen(viewModel = newsViewModel) // Передача ViewModel на екран
            }
            composable("categories") {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Екран категорій", style = MaterialTheme.typography.headlineMedium)
                }
            }
            composable("saved") {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Екран збережених новин", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: NewsViewModel) {
    // Підписка на StateFlow. `collectAsState` автоматично перекомпоновує HomeScreen
    // при зміні стану в ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    Column {
        CategoriesRow(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { category ->
                // Всі дії передаються до ViewModel. UI не приймає рішень.
                viewModel.selectCategory(category)
            }
        )
        // Передаємо відфільтрований список статей та функцію-обробник лайку.
        NewsList(
            articles = uiState.articles,
            onArticleLiked = { article ->
                viewModel.onArticleLiked(article)
            }
        )
    }
}

@Composable
fun CategoriesRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit // Функція для передачі події "нагору"
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}