package com.example.glucocareplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "novedades")
data class NovedadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: String,
    val hora: String,
    val nauseas: Boolean,
    val vomito: Boolean,
    val cuestaRespirar: Boolean,
    val dolorAbdomen: Boolean,
    val muchoSueno: Boolean,
    val debil: Boolean,
    val dolorCabeza: Boolean,
    val temblando: Boolean,
    val palido: Boolean,
    val sudando: Boolean,
    val otroSintoma: String
)