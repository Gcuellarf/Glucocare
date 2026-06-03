package com.example.glucocareplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucemiaDao {
    @Query("SELECT * FROM glucemias ORDER BY id DESC")
    fun obtenerHistorialGlucemias(): Flow<List<GlucemiaEntity>>

    @Insert
    suspend fun insertarGlucemia(glucemia: GlucemiaEntity)
}