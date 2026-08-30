package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.BluetoothSpeakerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothSpeakerDao {
    @Query("SELECT * FROM bluetooth_speakers ORDER BY zone ASC, name ASC")
    fun getAllSpeakers(): Flow<List<BluetoothSpeakerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeaker(speaker: BluetoothSpeakerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(speakers: List<BluetoothSpeakerEntity>)

    @Update
    suspend fun updateSpeaker(speaker: BluetoothSpeakerEntity)

    @Query("UPDATE bluetooth_speakers SET isConnected = :connected WHERE id = :speakerId")
    suspend fun updateConnectionState(speakerId: String, connected: Boolean)

    @Query("UPDATE bluetooth_speakers SET volume = :volume WHERE id = :speakerId")
    suspend fun updateVolume(speakerId: String, volume: Int)

    @Delete
    suspend fun deleteSpeaker(speaker: BluetoothSpeakerEntity)
}
