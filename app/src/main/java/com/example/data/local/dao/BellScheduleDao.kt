package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.BellScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BellScheduleDao {
    @Query("SELECT * FROM bell_schedules ORDER BY dayOfWeek ASC, time ASC")
    fun getAllSchedules(): Flow<List<BellScheduleEntity>>

    @Query("SELECT * FROM bell_schedules WHERE dayOfWeek = :dayOfWeek AND presetMode = :presetMode ORDER BY time ASC")
    fun getSchedulesForDayAndPreset(dayOfWeek: Int, presetMode: String): Flow<List<BellScheduleEntity>>

    @Query("SELECT * FROM bell_schedules WHERE isEnabled = 1 ORDER BY dayOfWeek ASC, time ASC")
    fun getEnabledSchedules(): Flow<List<BellScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BellScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<BellScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: BellScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: BellScheduleEntity)

    @Query("DELETE FROM bell_schedules WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM bell_schedules")
    suspend fun clearAll()
}
