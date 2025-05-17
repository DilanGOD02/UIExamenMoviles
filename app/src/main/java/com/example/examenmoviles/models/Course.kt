package com.example.examenmoviles.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Courses")
data class Course(
    @PrimaryKey val id: Int? = null,
    @ColumnInfo val name: String,
    @ColumnInfo val description: String,
    @ColumnInfo val imageUrl: String?,
    @ColumnInfo val schedule: String,
    @ColumnInfo val professor: String
)
