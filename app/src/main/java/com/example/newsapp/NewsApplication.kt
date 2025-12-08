package com.example.newsapp

import android.app.Application
import timber.log.Timber

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Ініціалізація Timber
        Timber.plant(Timber.DebugTree())

        // 2. Підключення нашого глобального перехоплювача помилок
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this, defaultHandler))
    }
}