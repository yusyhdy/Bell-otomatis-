package com.example.data.local.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.BellScheduleEntity
import com.example.data.local.entity.BluetoothSpeakerEntity
import com.example.data.local.entity.MicrocontrollerNodeEntity
import com.example.data.local.entity.PresetModeEntity

@Database(
    entities = [
        BellScheduleEntity::class,
        MicrocontrollerNodeEntity::class,
        ActivityLogEntity::class,
        BluetoothSpeakerEntity::class,
        PresetModeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bellScheduleDao(): BellScheduleDao
    abstract fun microcontrollerNodeDao(): MicrocontrollerNodeDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun bluetoothSpeakerDao(): BluetoothSpeakerDao
    abstract fun presetModeDao(): PresetModeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_bell_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
