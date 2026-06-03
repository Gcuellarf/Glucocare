package com.example.glucocareplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val username: String, // Clave única de ingreso
    val nombre: String,
    val apellido: String,
    val correo: String,
    val contrasena: String,
    val sexo: String,
    val peso: Double,
    val altura: Int,
    val fechaNacimiento: String,
    val contactoEmergencia: String
)