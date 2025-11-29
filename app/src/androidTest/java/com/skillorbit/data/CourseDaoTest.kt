package com.skillorbit.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: CourseDao

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.courseDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun insertAndList() = runBlocking {
        val c = Course(1, "Test", "Backend", 10, 3, "", CourseStatus.EM_ANDAMENTO)
        dao.insert(c)
        val list = dao.getAll().first()
        assertEquals(1, list.size)
        assertEquals("Test", list[0].title)
    }

    @Test
    fun updateAndDelete() = runBlocking {
        val c = Course(2, "Update", "Frontend", 8, 1, "", CourseStatus.EM_ANDAMENTO)
        dao.insert(c)
        dao.update(c.copy(completedLessons = 5))
        var item = dao.getById(2)!!
        assertEquals(5, item.completedLessons)
        dao.delete(item)
        val list = dao.getAll().first()
        assertEquals(0, list.size)
    }
}

