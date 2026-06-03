package com.example.glucocareplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Query("SELECT * FROM citas ORDER BY hora ASC")
    fun obtenerTodasLasCitas(): Flow<List<CitaEntity>>

    @Insert
    suspend fun insertarCita(cita: CitaEntity)
}