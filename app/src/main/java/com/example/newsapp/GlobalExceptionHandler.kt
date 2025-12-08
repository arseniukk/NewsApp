package com.example.newsapp

import android.content.Context
import android.content.Intent
import android.util.Log
import timber.log.Timber
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        // 1. Логуємо помилку через Timber (професійний підхід)
        Timber.e(exception, "🔥 CRITICAL ERROR CAUGHT! 🔥")

        // Також дублюємо в звичайний лог для надійності
        Log.e("NewsAppCrash", "Додаток впав через: ${exception.message}")

        // 2. Тут можна було б зберегти лог у файл або відправити на сервер

        // 3. Спробуємо перезапустити додаток (або просто коректно закрити)
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Не вдалося перезапустити додаток")
        }

        // 4. Викликаємо стандартний обробник (щоб система знала, що стався креш)
        // або примусово завершуємо процес.
        exitProcess(2)
    }
}

