package com.example.newsapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

object MqttManager {
    private const val BROKER_URL = "tcp://broker.hivemq.com:1883"
    private const val TOPIC = "newsapp/alert/tv21" // Унікальний топік для вашої групи
    private const val TAG = "MqttManager"

    // Створюємо клієнт у пам'яті
    private val client = MqttClient(BROKER_URL, UUID.randomUUID().toString(), MemoryPersistence())

    suspend fun connectAndPublish(messageText: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Підключення (якщо ще не підключено)
                if (!client.isConnected) {
                    val options = MqttConnectOptions().apply {
                        isCleanSession = true
                        connectionTimeout = 10
                    }
                    client.connect(options)
                    Log.d(TAG, "Connected to MQTT Broker")
                }

                // 2. Створення повідомлення
                val message = MqttMessage(messageText.toByteArray())
                message.qos = 1 // Quality of Service 1 (гарантована доставка)

                // 3. Публікація
                client.publish(TOPIC, message)
                Log.d(TAG, "Message published: $messageText")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error publishing message", e)
                false
            }
        }
    }
}