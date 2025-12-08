package com.example.newsapp.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newsapp.NewsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: NewsViewModel) {
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val savedArticlesCount by viewModel.savedArticles.collectAsState()
    val likedArticlesCount by viewModel.likedArticleIds.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Слухаємо події MQTT з ViewModel
    LaunchedEffect(Unit) {
        viewModel.mqttStatus.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Мої активність та IoT") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- СЕКЦІЯ ТЕСТУВАННЯ КРЕШІВ (ЗАВДАННЯ 24) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, contentDescription = "Crash")
                        Spacer(Modifier.width(8.dp))
                        Text("Crash Test", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Натисніть кнопку нижче, щоб перевірити роботу GlobalExceptionHandler (додаток перезапуститься).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Штучно викликаємо критичну помилку
                            throw RuntimeException("Test Crash from Dashboard!")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("💀 Викликати помилку")
                    }
                }
            }

            // --- IoT СЕКЦІЯ (ЗАВДАННЯ 20) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "IoT")
                        Spacer(Modifier.width(8.dp))
                        Text("Розумний дім", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Інтеграція з системою освітлення. Натисніть, щоб імітувати термінову новину.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.sendSmartHomeAlert() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("🚨 Надіслати сигнал тривоги (MQTT)")
                    }
                }
            }

            // --- Секція з круговою діаграмою (ЗАВДАННЯ 14) ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Збережені статті за категоріями", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    if (categoryCounts.isEmpty()) {
                        Text("Немає збережених статей для аналізу.")
                    } else {
                        DonutChart(data = categoryCounts)
                    }
                }
            }

            // --- Секція з індикатором прогресу (ЗАВДАННЯ 14) ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Співвідношення активності", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    val savedCount = savedArticlesCount.size
                    val likedCount = likedArticlesCount.size
                    val progress = if (savedCount > 0) likedCount.toFloat() / savedCount.toFloat() else 0f

                    Text("Лайкнуто / Збережено", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("$likedCount лайків / $savedCount збережених", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}


@Composable
fun DonutChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }.toFloat()
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Сам графік ---
        Box(
            modifier = Modifier
                .size(150.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                data.forEachIndexed { index, pair ->
                    val sweepAngle = (pair.second / total) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * animatedProgress.value,
                        useCenter = false,
                        style = Stroke(width = 35f, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // --- Легенда до графіка ---
        Column {
            data.forEachIndexed { index, (category, count) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colors[index % colors.size], CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$category ($count)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}