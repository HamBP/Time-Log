package me.algosketch.timelog.di

import android.content.Context
import androidx.room.Room
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
            .build()

    @Provides
    fun provideLogTypeDao(db: AppDatabase): LogTypeDao = db.logTypeDao()

    @Provides
    fun provideLogSessionDao(db: AppDatabase): LogSessionDao = db.logSessionDao()
}
