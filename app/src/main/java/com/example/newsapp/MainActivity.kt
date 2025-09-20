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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newsapp.ui.theme.NewsAppTheme

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

    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Головна", "Категорії", "Збережене")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Мої Новини") },
            )
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
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    restoreState = true
                                }
                                "Категорії" -> navController.navigate("categories") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    restoreState = true
                                }
                                "Збережене" -> navController.navigate("saved") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController, startDestination = "home", modifier = Modifier.padding(paddingValues)) {
            composable("home") {
                HomeScreen()
            }
            composable("categories") {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    Text("Екран категорій", style = MaterialTheme.typography.headlineMedium)
                }
            }
            composable("saved") {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    Text("Екран збережених новин", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val categories = listOf("Усі", "Технології", "Спорт", "Політика", "Наука", "Розваги", "Бізнес")

    Column {
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = false,
                    onClick = { /* TODO: Фільтрувати новини за категорією */ },
                    label = { Text(category) }
                )
            }
        }

        NewsList(sampleArticles)
    }
}