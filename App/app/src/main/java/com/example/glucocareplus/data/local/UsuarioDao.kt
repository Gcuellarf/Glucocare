package com.example.glucocareplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE username = :username LIMIT 1")
    suspend fun obtenerUsuarioPorUsername(username: String): UsuarioEntity?

    @Insert
    suspend fun registrarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE username = :username AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(username: String, contrasena: String): UsuarioEntity?
}