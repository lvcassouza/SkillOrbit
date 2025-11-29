package com.skillorbit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skillorbit.data.Course
import com.skillorbit.data.CourseStatus
import com.skillorbit.repo.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardState(
    val userName: String = "",
    val courses: List<Course> = emptyList(),
    val overallProgress: Float = 0f,
    val suggestion: String? = null,
    val isAddCourseDialogOpen: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM" // SYSTEM, LIGHT, DARK
)

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.userNameFlow,
                repository.coursesFlow,
                repository.onboardingCompletedFlow,
                repository.notificationsEnabledFlow,
                repository.themeModeFlow
            ) { name, courses, onboarded, notifications, theme ->
                val safeName = name ?: ""
                val progress = calculateOverallProgress(courses)
                val suggestion = buildSuggestion(courses)
                DashboardState(
                    userName = safeName,
                    courses = courses,
                    overallProgress = progress,
                    suggestion = suggestion,
                    isAddCourseDialogOpen = _state.value.isAddCourseDialogOpen,
                    onboardingCompleted = onboarded,
                    notificationsEnabled = notifications,
                    themeMode = theme
                )
            }.collect { _state.value = it }
        }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch { repository.saveUserName(name) }
    }

    fun openAddCourseDialog() { _state.value = _state.value.copy(isAddCourseDialogOpen = true) }
    fun closeAddCourseDialog() { _state.value = _state.value.copy(isAddCourseDialogOpen = false) }

    fun addCourse(title: String, category: String, totalLessons: Int) {
        val course = Course(
            title = title.trim(),
            category = category.trim(),
            totalLessons = totalLessons.coerceAtLeast(0),
            completedLessons = 0,
            notes = "",
            status = CourseStatus.EM_ANDAMENTO
        )
        viewModelScope.launch {
            repository.insert(course)
            closeAddCourseDialog()
        }
    }

    fun updateCourseProgress(id: Long, completedLessons: Int, notes: String) {
        viewModelScope.launch {
            val current = repository.getById(id) ?: return@launch
            val total = current.totalLessons.coerceAtLeast(0)
            val completed = completedLessons.coerceIn(0, total)
            val status = when {
                completed >= total && total > 0 -> CourseStatus.CONCLUIDO
                completed == 0 -> CourseStatus.PAUSADO
                else -> CourseStatus.EM_ANDAMENTO
            }
            repository.update(
                current.copy(
                    completedLessons = completed,
                    notes = notes,
                    status = status
                )
            )
        }
    }

    fun deleteCourse(id: Long): Course? {
        var deleted: Course? = null
        viewModelScope.launch {
            val current = repository.getById(id) ?: return@launch
            deleted = current
            repository.delete(current)
        }
        return deleted
    }

    fun restoreCourse(course: Course) {
        viewModelScope.launch { repository.insert(course) }
    }

    private fun calculateOverallProgress(courses: List<Course>): Float {
        if (courses.isEmpty()) return 0f
        val ratios = courses.mapNotNull { c ->
            if (c.totalLessons > 0) c.completedLessons.toFloat() / c.totalLessons.toFloat() else null
        }
        if (ratios.isEmpty()) return 0f
        return ratios.average().toFloat().coerceIn(0f, 1f)
    }

    private fun buildSuggestion(courses: List<Course>): String? {
        val backendCount = courses.count { it.category.contains("Backend", ignoreCase = true) }
        val frontendCount = courses.count { it.category.contains("Frontend", ignoreCase = true) }
        val uiuxCount = courses.count { it.category.contains("UI/UX", ignoreCase = true) || it.category.contains("UI", ignoreCase = true) || it.category.contains("UX", ignoreCase = true) }
        return if (backendCount >= 2 && frontendCount == 0 && uiuxCount == 0) "Que tal explorar UI/UX?" else null
    }

    fun completeOnboarding() {
        viewModelScope.launch { repository.completeOnboarding() }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }
}

class CourseViewModelFactory(private val repository: CourseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CourseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
