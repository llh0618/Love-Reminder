package com.example.girlfriend.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.girlfriend.data.entity.Anniversary
import com.example.girlfriend.data.entity.Gift
import com.example.girlfriend.data.entity.Note

@Database(
    entities = [Anniversary::class, Note::class, Gift::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun anniversaryDao(): AnniversaryDao
    abstract fun noteDao(): NoteDao
    abstract fun giftDao(): GiftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "girlfriend_memo.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
