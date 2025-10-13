package com.example.newsapp

import android.content.Context
import androidx.room.*

@Database(
    entities = [
        ArticleEntity::class, // +++ Додано таблицю для кешу
        SavedArticleEntity::class,
        LikedArticleId::class
    ],
    version = 2, // +++ ЗБІЛЬШУЄМО ВЕРСІЮ БАЗИ ДАНИХ
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "news_app_database"
                )
                    // Дозволяє Room видалити та створити базу даних заново при зміні версії.
                    // Ідеально для розробки. У релізних додатках потрібні міграції.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}