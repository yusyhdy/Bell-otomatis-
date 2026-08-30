package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PresetModeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetModeDao {
    @Query("SELECT * FROM preset_modes ORDER BY id ASC")
    fun getAllPresets(): Flow<List<PresetModeEntity>>

    @Query("SELECT * FROM preset_modes WHERE isActive = 1 LIMIT 1")
    fun getActivePreset(): Flow<PresetModeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetModeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(presets: List<PresetModeEntity>)

    @Query("UPDATE preset_modes SET isActive = CASE WHEN id = :presetId THEN 1 ELSE 0 END")
    suspend fun setActivePreset(presetId: String)
}
