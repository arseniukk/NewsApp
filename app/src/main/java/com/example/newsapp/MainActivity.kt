package com.example.newsapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.newsapp.screens.AnalyticsScreen
import com.example.newsapp.screens.ArticleDetailScreen
import com.example.newsapp.screens.DashboardScreen
import com.example.newsapp.screens.HomeScreen
import com.example.newsapp.screens.SavedScreen
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
    val context = LocalContext.current
    val newsViewModel: NewsViewModel = viewModel(
        factory = NewsViewModelFactory(context.applicationContext as Application)
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val navItems = listOf(
        "Головна" to Screen.HomeScreen.route,
        "Збережене" to Screen.SavedScreen.route,
        "Аналітика" to Screen.AnalyticsScreen.route,
        "Дашборд" to Screen.DashboardScreen.route
    )
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.Default.Bookmark,
        Icons.Default.Analytics,
        Icons.Default.Dashboard
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        newsViewModel.snackbarEvent.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, (label, route) ->
                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
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

            composable(route = Screen.SavedScreen.route) {
                SavedScreen(
                    viewModel = newsViewModel,
                    onArticleClick = { articleId ->
                        navController.navigate(Screen.ArticleDetailScreen.createRoute(articleId))
                    }
                )
            }

            composable(route = Screen.AnalyticsScreen.route) {
                AnalyticsScreen(viewModel = newsViewModel)
            }

            composable(route = Screen.DashboardScreen.route) {
                DashboardScreen(viewModel = newsViewModel)
            }

            composable(
                route = Screen.ArticleDetailScreen.route,
                arguments = listOf(navArgument(ARTICLE_ID_ARG) { type = NavType.IntType })
                // Deep link видалено для спрощення, ви можете його повернути
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt(ARTICLE_ID_ARG)
                val article = articleId?.let { newsViewModel.getArticleById(it) }

                if (article != null) {
                    ArticleDetailScreen(
                        article = article,
                        viewModel = newsViewModel,
                        onNavigateUp = { navController.navigateUp() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Статтю не знайдено...")
                    }
                }
            }
        }
    }
}

class NewsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}