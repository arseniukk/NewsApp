package com.example.newsapp

import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

// Прибираємо ", Configuration.Provider"
class NewsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    // Ми повністю видалили override val workManagerConfiguration,
    // оскільки WorkManager може ініціалізуватися сам.

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<NewsSyncWorker>(
            6, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        // WorkManager ініціалізується "ледаче" при першому виклику getInstance
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "NewsSyncWorkName",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}