package com.example.examenmoviles.models

data class StudentDetails(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val courseId: Int,
    val courseName: String,
    val courseDescription: String,
    val courseSchedule: String,
    val courseProfessor: String,
    val courseImageUrl: String
)