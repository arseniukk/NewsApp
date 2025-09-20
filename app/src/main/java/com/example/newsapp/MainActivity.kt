package com.example.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.newsapp.screens.ArticleDetailScreen
import com.example.newsapp.screens.HomeScreen
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
                    NewsApp()
                }
            }
        }
    }
}

@Composable
fun NewsApp() {
    val navController = rememberNavController()
    val newsViewModel: NewsViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Підписка на події Snackbar з ViewModel
    LaunchedEffect(Unit) {
        newsViewModel.snackbarEvent.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
        }
    }

    // Стан для нижньої навігації
    var selectedItem by remember { mutableStateOf(0) }
    val navItems = listOf("Головна", "Категорії", "Збережене")
    val navIcons = listOf(Icons.Default.Home, Icons.Default.Category, Icons.Default.Bookmark)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            // Навігація для нижньої панелі. Поки що всі кнопки ведуть на головний екран.
                            navController.navigate(Screen.HomeScreen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        // NavHost тепер є контентом Scaffold і отримує його відступи
        NavHost(
            navController = navController,
            startDestination = Screen.HomeScreen.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(route = Screen.HomeScreen.route) {
                HomeScreen(
                    viewModel = newsViewModel,
                    onArticleClick = { articleId ->
                        navController.navigate(Screen.ArticleDetailScreen.createRoute(articleId))
                    }
                )
            }

            composable(
                route = Screen.ArticleDetailScreen.route,
                arguments = listOf(navArgument(ARTICLE_ID_ARG) { type = NavType.IntType }),
                deepLinks = listOf(navDeepLink { uriPattern = "https://www.mynewsapp.com/article/{$ARTICLE_ID_ARG}" })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt(ARTICLE_ID_ARG)
                val article = articleId?.let { newsViewModel.getArticleById(it) }

                if (article != null) {
                    ArticleDetailScreen(
                        article = article,
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}