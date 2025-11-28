package com.skillorbit.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [Course::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
}

object Converters {
    @TypeConverter
    fun toStatus(value: String): CourseStatus = CourseStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(status: CourseStatus): String = status.name
}

