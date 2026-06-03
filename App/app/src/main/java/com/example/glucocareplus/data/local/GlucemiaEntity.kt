package com.example.glucocareplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucemias")
data class GlucemiaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val valor: Int,              // El número que arroja el glucómetro (mg/dL)
    val momento: String,         // Obligatorio: "ANTES_DE_COMER" o "DESPUES_DE_COMER"
    val fecha: String,           // Fecha del registro (Ej: "01/06/2026")
    val hora: String,            // Hora del registro (Ej: "10:15 AM")
    val estadoSemaforo: String   // "HIPO", "NORMAL" o "HIPER" para facilitar estadísticas luego
)