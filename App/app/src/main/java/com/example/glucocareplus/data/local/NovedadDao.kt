package com.example.glucocareplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NovedadDao {
    @Query("SELECT * FROM novedades ORDER BY id DESC")
    fun obtenerHistorialNovedades(): Flow<List<NovedadEntity>>

    @Insert
    suspend fun insertarNovedad(novedad: NovedadEntity)
}