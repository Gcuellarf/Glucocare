package com.example.glucocareplus.ui.screens

import android.widget.Toast // <--- Asegúrate de importar Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext // <--- Importante para el Toast
import com.example.glucocareplus.R
import com.example.glucocareplus.ui.viewmodels.UsuarioViewModel // <--- Tu ViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: UsuarioViewModel, // <--- 1. INYECTAMOS EL VIEWMODEL AQUÍ
    onLoginSuccess: () -> Unit,   // <--- 2. CAMBIAMOS EL EVENTO A ÉXITO
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current // <--- Contexto para mostrar alertas

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Logotipo de GlucoCarePlus",
            modifier = Modifier.size(175.dp)
        )

        Text(
            text = "Inicio de Sesión",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "¡Bienvenido nuevamente!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Escribe aquí tu nombre de usuario") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Ícono de usuario")
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Escribe tu contraseña") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Ícono de candado")
            },
            trailingIcon = {
                val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 32.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onForgotPasswordClick() }
                    .padding(vertical = 4.dp)
            )
        }

        // --- BOTÓN ADAPTADO A LA BASE DE DATOS ---
        Button(
            onClick = {
                // Ejecutamos la validación en el hilo secundario mediante el ViewModel
                viewModel.validarInicioSesion(username, password) { exitoso, mensaje ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                    if (exitoso) {
                        onLoginSuccess() // Rompe el bloqueo de Auth y activa la PantallaPrincipalApp
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Ingresar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¿No tienes cuenta?",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable { onRegisterClick() }
                .padding(all = 8.dp)
        )
    }
}