package com.example.newsapp.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsapp.Article
import com.example.newsapp.LocationUtils
import com.example.newsapp.NewsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val livePrice by viewModel.livePrice.collectAsState()

    val context = LocalContext.current
    var locationLatLng by remember { mutableStateOf<LatLng?>(null) }
    val bluetoothManager = remember { context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager }
    val bluetoothAdapter: BluetoothAdapter? = remember { bluetoothManager?.adapter }

    // +++ ОТРИМУЄМО ДОСТУП ДО СИСТЕМИ ВІБРАЦІЇ +++
    val haptics = LocalHapticFeedback.current

    val enableBluetoothLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    fun shareArticle(articleToShare: Article) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${articleToShare.title}\n\n(Посилання-заглушка для демонстрації)")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            if (bluetoothAdapter?.isEnabled == true) {
                shareArticle(article)
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            }
        }
    }

    DisposableEffect(article.id) {
        viewModel.startPriceMonitoring()
        val geocodingJob = CoroutineScope(Dispatchers.Main).launch {
            locationLatLng = LocationUtils.getLatLngFromArticle(context, article)
        }
        onDispose {
            viewModel.stopPriceMonitoring()
            geocodingJob.cancel()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.category, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                        } else {
                            if (bluetoothAdapter?.isEnabled == true) {
                                shareArticle(article)
                            } else {
                                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                enableBluetoothLauncher.launch(enableBtIntent)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Поділитися")
                    }
                    IconButton(onClick = { viewModel.toggleLikeArticle(article) }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Лайк"
                        )
                    }
                    // +++ ДОДАЄМО ВІБРАЦІЮ ПРИ ЗБЕРЕЖЕННІ +++
                    IconButton(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleSaveArticle(article)
                    }) {
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

            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Live Bitcoin Price (BTC-USD)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (livePrice != null) "$$livePrice" else "Connecting...",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (locationLatLng != null) {
                Spacer(Modifier.height(16.dp))
                Text("Місце події на карті", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(locationLatLng!!, 5f)
                }

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = locationLatLng!!),
                        title = article.author,
                        snippet = "Джерело новини"
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}