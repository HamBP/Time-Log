package me.algosketch.timelog.data

import me.algosketch.timelog.data.local.dao.LogSessionDao
import me.algosketch.timelog.data.local.dao.LogTypeDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val logTypeDao: LogTypeDao,
    private val logSessionDao: LogSessionDao,
) {

}