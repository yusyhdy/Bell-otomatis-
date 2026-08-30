package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MicrocontrollerNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MicrocontrollerNodeDao {
    @Query("SELECT * FROM microcontroller_nodes ORDER BY id ASC")
    fun getAllNodes(): Flow<List<MicrocontrollerNodeEntity>>

    @Query("SELECT * FROM microcontroller_nodes WHERE id = :id LIMIT 1")
    fun getNodeById(id: String): Flow<MicrocontrollerNodeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNode(node: MicrocontrollerNodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<MicrocontrollerNodeEntity>)

    @Query("UPDATE microcontroller_nodes SET amplifierState = :state WHERE id = :nodeId")
    suspend fun updateAmplifierState(nodeId: String, state: Boolean)

    @Query("UPDATE microcontroller_nodes SET lastNtpSyncMillis = :syncMillis, ntpDriftMs = :driftMs WHERE id = :nodeId")
    suspend fun updateNtpSync(nodeId: String, syncMillis: Long, driftMs: Long)

    @Query("UPDATE microcontroller_nodes SET isOnline = :isOnline, mqttConnected = :mqttConnected WHERE id = :nodeId")
    suspend fun updateConnectivity(nodeId: String, isOnline: Boolean, mqttConnected: Boolean)
}
