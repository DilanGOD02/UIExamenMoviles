package com.example.examenmoviles.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.examenmoviles.models.Course

@Dao
interface CourseDao {

    @Query("SELECT * FROM Courses")
    suspend fun getAllCourses(): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course)

    @Delete
    suspend fun delete(course: Course)

    @Query("DELETE FROM Courses WHERE id = :courseId")
    suspend fun deleteById(courseId: Int)

    @Query("SELECT * FROM Courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: Int): Course?
}