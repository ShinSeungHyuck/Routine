package com.example.routine.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Routine::class, RoutineCompletion::class], version = 1, exportSchema = false)
abstract class RoutineDatabase : RoomDatabase() {

    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: RoutineDatabase? = null

        fun getDatabase(context: Context): RoutineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoutineDatabase::class.java,
                    "routine_database"
                )
                .fallbackToDestructiveMigration() // Wipes and rebuilds instead of migrating if no Migration object.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}