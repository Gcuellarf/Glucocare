package com.example.glucocareplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos")
data class MedicamentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val dosis: String,
    val horaProgramada: String,
    val estado: String,
    val horaTomada: String? = null
)