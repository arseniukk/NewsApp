package com.example.newsapp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NewsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).articleDao()
        val repository = NewsRepository(dao)

        return try {
            // Оновлюємо кеш для основної категорії у фоновому режимі
            repository.refreshArticles("general")
            Result.success()
        } catch (e: Exception) {
            Result.retry() // Якщо помилка мережі, спробуємо пізніше
        }
    }
}