package com.example.glucocareplus.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    // Obtiene todos los medicamentos del día ordenados por hora
    @Query("SELECT * FROM medicamentos ORDER BY horaProgramada ASC")
    fun obtenerTodosLosMedicamentos(): Flow<List<MedicamentoEntity>>

    // Inserta un nuevo medicamento (El botón + usará esto)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMedicamento(medicamento: MedicamentoEntity)

    // Actualiza el estado de la toma (Cuando presionan "Tomar" o "Sí, me la tomé")
    @Query("UPDATE medicamentos SET estado = :nuevoEstado, horaTomada = :horaTomada WHERE id = :id")
    suspend fun actualizarEstadoToma(id: Int, nuevoEstado: String, horaTomada: String?)
}