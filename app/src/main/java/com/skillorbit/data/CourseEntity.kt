package com.skillorbit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CourseStatus { EM_ANDAMENTO, CONCLUIDO, PAUSADO }

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val category: String,
    val totalLessons: Int,
    val completedLessons: Int,
    val notes: String,
    val status: CourseStatus
)

