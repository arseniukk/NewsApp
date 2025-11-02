package com.example.newsapp.network

import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*

private const val TAG = "PriceWebSocket" // Тег для фільтрації в Logcat

@Serializable
data class TickerMessage(
    val type: String,
    val product_id: String,
    val price: String? = null
)

class PriceWebSocketListener {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun startListening(): Flow<String> = callbackFlow {
        Log.d(TAG, "Починаємо прослуховування...")
        val request = Request.Builder()
            .url("wss://ws-feed.exchange.coinbase.com")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "З'єднання відкрито!")
                val subscribeMessage = """
                {
                    "type": "subscribe",
                    "product_ids": ["BTC-USD"],
                    "channels": ["ticker"]
                }
                """.trimIndent()
                webSocket.send(subscribeMessage)
                Log.d(TAG, "Надіслано запит на підписку.")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Отримано повідомлення: $text")
                try {
                    val message = json.decodeFromString<TickerMessage>(text)
                    if (message.type == "ticker" && message.price != null) {
                        trySend(message.price).isSuccess
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Помилка парсингу JSON", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "З'єднання закривається: $code $reason")
                webSocket.close(1000, null)
                channel.close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Помилка з'єднання!", t)
                channel.close(t)
            }
        }

        webSocket = client.newWebSocket(request, listener)

        awaitClose {
            Log.d(TAG, "Flow закрито, зупиняємо WebSocket.")
            webSocket?.close(1000, "Flow closed")
        }
    }

    fun stopListening() {
        webSocket?.close(1000, "Manually stopped")
    }
}