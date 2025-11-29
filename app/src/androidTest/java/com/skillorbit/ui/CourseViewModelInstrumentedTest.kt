package com.skillorbit.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skillorbit.data.AppDatabase
import com.skillorbit.data.Course
import com.skillorbit.data.CourseStatus
import com.skillorbit.prefs.UserPreferences
import com.skillorbit.repo.CourseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseViewModelInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: CourseRepository
    private lateinit var vm: CourseViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val prefs = UserPreferences(context)
        repository = CourseRepository(db.courseDao(), prefs)
        vm = CourseViewModel(repository)
    }

    @After
    fun tearDown() { db.close() }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun suggestionAppearsForBackendOnly() = runTest {
        repository.insert(Course(1, "Kotlin", "Backend", 10, 5, "", CourseStatus.EM_ANDAMENTO))
        repository.insert(Course(2, "Spring", "Backend", 8, 2, "", CourseStatus.EM_ANDAMENTO))
        val state = vm.state.value
        assertEquals("Que tal explorar UI/UX?", state.suggestion)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun overallProgressIsAverage() = runTest {
        repository.insert(Course(1, "A", "Backend", 10, 5, "", CourseStatus.EM_ANDAMENTO))
        repository.insert(Course(2, "B", "Frontend", 10, 0, "", CourseStatus.PAUSADO))
        val progress = vm.state.value.overallProgress
        assertEquals(0.25f, progress)
    }
}

