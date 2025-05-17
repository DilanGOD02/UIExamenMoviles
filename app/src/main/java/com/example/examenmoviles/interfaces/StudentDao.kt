package com.example.examenmoviles.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.examenmoviles.models.Student

@Dao
interface StudentDao {
    @Query("SELECT * FROM Students")
    suspend fun getAll(): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Student>)

    @Query("DELETE FROM Students")
    suspend fun clearAll()
}