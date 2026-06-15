package me.algosketch.timelog.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.algosketch.timelog.data.local.AppDatabase
import me.algosketch.timelog.data.local.dao.LogSessionDao
import me.algosketch.timelog.data.local.dao.LogTypeDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "timelog.db")
            .fallbackToDestructiveMigration()
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("INSERT INTO log_types (name, colorHex, icon, sortOrder, isDefault, isActive, includeEfficiency) VALUES ('일하는 중', '#4ADE80', '▶', 0, 1, 1, 1)")
                    db.execSQL("INSERT INTO log_types (name, colorHex, icon, sortOrder, isDefault, isActive, includeEfficiency) VALUES ('쉬는 중', '#FB923C', '☕', 1, 1, 1, 0)")
                }
            })
            .build()

    @Provides
    fun provideLogTypeDao(db: AppDatabase): LogTypeDao = db.logTypeDao()

    @Provides
    fun provideLogSessionDao(db: AppDatabase): LogSessionDao = db.logSessionDao()
}
