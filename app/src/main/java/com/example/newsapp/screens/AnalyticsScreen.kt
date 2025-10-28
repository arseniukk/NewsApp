package com.example.newsapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newsapp.NewsViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: NewsViewModel) {
    // Підписуємося на дані для графіка з ViewModel
    val categoryCounts by viewModel.categoryCounts.collectAsState()

    // Створюємо модель даних для графіка
    val chartEntryModelProducer = ChartEntryModelProducer(
        categoryCounts.mapIndexed { index, pair ->
            entryOf(index.toFloat(), pair.second) // x = індекс, y = кількість
        }
    )

    // Форматуємо підписи по осі X (назви категорій)
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        categoryCounts.getOrNull(value.toInt())?.first ?: ""
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Аналітика новин") })
        }
    ) { paddingValues ->
        if (categoryCounts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text("Немає даних для аналітики. Завантажте новини на головному екрані.")
            }
        } else {
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                Text(
                    "Кількість статей за категоріями",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))
                Chart(
                    chart = columnChart(),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = bottomAxisValueFormatter,
                        labelRotationDegrees = 45f // Повертаємо підписи для кращої читабельності
                    ),
                )
            }
        }
    }
}

