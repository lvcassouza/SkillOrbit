package com.skillorbit.repo

import com.skillorbit.data.Course
import com.skillorbit.data.CourseDao
import com.skillorbit.prefs.UserPreferences
import kotlinx.coroutines.flow.Flow

class CourseRepository(
    private val dao: CourseDao,
    private val prefs: UserPreferences
) {
    val coursesFlow: Flow<List<Course>> = dao.getAll()
    val userNameFlow: Flow<String?> = prefs.userNameFlow

    suspend fun insert(course: Course): Long = dao.insert(course)
    suspend fun update(course: Course) = dao.update(course)
    suspend fun delete(course: Course) = dao.delete(course)
    suspend fun getById(id: Long): Course? = dao.getById(id)

    suspend fun saveUserName(name: String) = prefs.setUserName(name)
}

