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
            // TODO: 앱 기획 안정화 작업이 끝나면 아래와 같은 파괴적인 코드를 수정할 것
            .fallbackToDestructiveMigration()
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    val cursor = db.query("SELECT COUNT(*) FROM log_types")
                    cursor.moveToFirst()
                    val isEmpty = cursor.getInt(0) == 0
                    cursor.close()
                    if (isEmpty) seedDefaultTypes(db)
                }
            })
            .build()

    private fun seedDefaultTypes(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO log_types (name, colorHex, icon, sortOrder, isDefault, isActive, includeEfficiency) VALUES ('일하는 중', '#4ADE80', 'play_arrow', 0, 1, 1, 1)")
        db.execSQL("INSERT INTO log_types (name, colorHex, icon, sortOrder, isDefault, isActive, includeEfficiency) VALUES ('쉬는 중', '#FB923C', 'free_breakfast', 1, 1, 1, 0)")
    }

    @Provides
    fun provideLogTypeDao(db: AppDatabase): LogTypeDao = db.logTypeDao()

    @Provides
    fun provideLogSessionDao(db: AppDatabase): LogSessionDao = db.logSessionDao()
}
