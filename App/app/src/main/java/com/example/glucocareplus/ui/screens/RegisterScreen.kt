package com.example.glucocareplus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glucocareplus.ui.viewmodels.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: UsuarioViewModel, // Inyectamos el ViewModel
    onRegisterSuccessClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var contactoEmergencia by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- CABECERA ---
        Text(
            text = "Crear Cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Completa tus datos para comenzar tu control",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- SECCIÓN 1: DATOS PERSONALES ---
        Text(
            text = "Datos Personales",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre Completo *") },
            placeholder = { Text("Escribe tu nombre") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            placeholder = { Text("Escribe tu apellido") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico *") },
            placeholder = { Text("ejemplo@correo.com") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // --- SECCIÓN 2: DATOS DE LA CUENTA ---
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "Datos de Inicio de Sesión",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de Usuario (Único) *") },
            placeholder = { Text("Ej: miguel123") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña *") },
            placeholder = { Text("Crea una contraseña segura") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // --- SECCIÓN 3: DATOS FÍSICOS Y SALUD ---
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "Información Física",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = sexo,
            onValueChange = { sexo = it },
            label = { Text("Sexo") },
            placeholder = { Text("Masculino, Femenino u Otro") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso (kg)") },
                placeholder = { Text("Ej: 75") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = altura,
                onValueChange = { altura = it },
                label = { Text("Altura (cm)") },
                placeholder = { Text("Ej: 170") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = { fechaNacimiento = it },
            label = { Text("Fecha de Nacimiento") },
            placeholder = { Text("DD/MM/AAAA") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // --- SECCIÓN 4: SEGURIDAD ---
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "Contacto de Emergencia",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = contactoEmergencia,
            onValueChange = { contactoEmergencia = it },
            label = { Text("Teléfono de un Familiar o Cuidador") },
            placeholder = { Text("Ej: 3123456789") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // --- BOTÓN REGISTRARSE (Acción conectada a la BD) ---
        Button(
            onClick = {
                viewModel.registrarNuevoUsuario(
                    nombre = nombre, apellido = apellido, correo = correo,
                    username = username, contrasena = password, sexo = sexo,
                    pesoStr = peso, alturaStr = altura, fechaNacimiento = fechaNacimiento,
                    contactoEmergencia = contactoEmergencia
                ) { exitoso, mensaje ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    if (exitoso) {
                        onRegisterSuccessClick() // Navega al flujo interno o login
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Finalizar Registro", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackToLoginClick) {
            Text(text = "Ya tengo cuenta. Iniciar Sesión", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}