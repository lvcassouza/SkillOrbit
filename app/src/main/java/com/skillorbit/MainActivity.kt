package com.skillorbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.skillorbit.data.AppDatabase
import com.skillorbit.prefs.UserPreferences
import com.skillorbit.repo.CourseRepository
import com.skillorbit.ui.CourseViewModel
import com.skillorbit.ui.CourseViewModelFactory
import com.skillorbit.ui.SkillOrbitApp
import com.skillorbit.ui.SkillOrbitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "skillorbit.db").build()
        val prefs = UserPreferences(applicationContext)
        val repository = CourseRepository(db.courseDao(), prefs)
        val factory = CourseViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[CourseViewModel::class.java]
        setContent {
            SkillOrbitTheme {
                SkillOrbitApp(viewModel)
            }
        }
    }
}

