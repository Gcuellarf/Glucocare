package com.example.glucocareplus.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glucocareplus.data.local.UsuarioDao
import com.example.glucocareplus.data.local.UsuarioEntity
import kotlinx.coroutines.launch

class UsuarioViewModel(private val usuarioDao: UsuarioDao) : ViewModel() {

    // Variable reactiva expuesta para almacenar el nombre de usuario activo
    var usuarioLogueado by mutableStateOf("")
        private set

    fun registrarNuevoUsuario(
        nombre: String, apellido: String, correo: String,
        username: String, contrasena: String, sexo: String,
        pesoStr: String, alturaStr: String, fechaNacimiento: String,
        contactoEmergencia: String,
        onResultado: (exitoso: Boolean, mensaje: String) -> Unit
    ) {
        viewModelScope.launch {
            // 1. Validar que no haya campos cruciales vacíos
            if (nombre.isBlank() || username.isBlank() || contrasena.isBlank() || correo.isBlank()) {
                onResultado(false, "Por favor completa los campos principales.")
                return@launch
            }

            // 2. Verificar disponibilidad del nombre de usuario
            val usuarioExistente = usuarioDao.obtenerUsuarioPorUsername(username.trim())
            if (usuarioExistente != null) {
                onResultado(false, "El nombre de usuario ya está en uso.")
                return@launch
            }

            // 3. Conversión segura de tipos de datos numéricos
            val peso = pesoStr.toDoubleOrNull() ?: 0.0
            val altura = alturaStr.toIntOrNull() ?: 0

            // 4. Crear entidad y guardar
            val nuevoUsuario = UsuarioEntity(
                username = username.trim(),
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                correo = correo.trim(),
                contrasena = contrasena, // En producción se recomienda encriptar
                sexo = sexo.trim(),
                peso = peso,
                altura = altura,
                fechaNacimiento = fechaNacimiento.trim(),
                contactoEmergencia = contactoEmergencia.trim()
            )

            try {
                usuarioDao.registrarUsuario(nuevoUsuario)
                onResultado(true, "¡Cuenta creada con éxito!")
            } catch (e: Exception) {
                onResultado(false, "Error al guardar en la base de datos.")
            }
        }
    }

    fun validarInicioSesion(
        usernameStr: String,
        contrasenaStr: String,
        onResultado: (exitoso: Boolean, mensaje: String) -> Unit
    ) {
        viewModelScope.launch {
            val user = usernameStr.trim()
            val pass = contrasenaStr

            if (user.isBlank() || pass.isBlank()) {
                onResultado(false, "Por favor escribe tus datos completos.")
                return@launch
            }

            // Consultamos de forma asíncrona en la BD
            val usuarioEncontrado = usuarioDao.iniciarSesion(user, pass)

            if (usuarioEncontrado != null) {
                // GUARDAMOS EL USUARIO: Al ser exitoso, guardamos el username procesado
                usuarioLogueado = user
                onResultado(true, "¡Bienvenido de nuevo, ${usuarioEncontrado.nombre}!")
            } else {
                onResultado(false, "Usuario o contraseña incorrectos.")
            }
        }
    }
}