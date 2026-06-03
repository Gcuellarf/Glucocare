package com.example.glucocareplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas")
data class CitaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,          // Ej: "Cita con Endocrinología"
    val medico: String,          // Ej: "Dr. Carlos Ordóñez"
    val lugar: String,           // Ej: "Hospital Susana López"
    val hora: String,            // Ej: "09:30 AM"
    val fechaIso: String         // Formato estandarizado para consultas rápidas: "YYYY-MM-DD" (Ej: "2026-06-15")
)