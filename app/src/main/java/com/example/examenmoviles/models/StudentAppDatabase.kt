package com.example.examenmoviles.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.examenmoviles.interfaces.StudentDao
import kotlin.jvm.java

@Database(entities = [Student::class], version = 1)
abstract class StudentAppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var INSTANCE: StudentAppDatabase? = null

        fun getInstance(context: Context): StudentAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentAppDatabase::class.java,
                    "students_database"
                )
                    // .fallbackToDestructiveMigration() // descomenta si estás haciendo pruebas y cambias el modelo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
