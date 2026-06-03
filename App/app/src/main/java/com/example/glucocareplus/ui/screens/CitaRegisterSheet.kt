package com.example.glucocareplus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaRegisterSheet(
    showSheet: Boolean,
    fechaSeleccionadaIso: String, // Recibe la fecha que el usuario tocó en el calendario
    onDismissRequest: () -> Unit,
    onGuardarCita: (titulo: String, medico: String, lugar: String, hora: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var medico by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Agendar Nueva Cita", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Para el día: $fechaSeleccionadaIso", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Especialidad o Motivo *") },
                    placeholder = { Text("Ej: Control de Endocrinología") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = medico,
                    onValueChange = { medico = it },
                    label = { Text("Nombre del Médico") },
                    placeholder = { Text("Ej: Dr. Carlos Ordóñez") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lugar,
                    onValueChange = { lugar = it },
                    label = { Text("Centro Médico o Dirección *") },
                    placeholder = { Text("Ej: Hospital Susana López") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora de la Cita *") },
                    placeholder = { Text("Ej: 09:30 AM") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (titulo.isNotBlank() && lugar.isNotBlank() && hora.isNotBlank()) {
                            onGuardarCita(titulo, medico, lugar, hora)
                            titulo = ""; medico = ""; lugar = ""; hora = ""
                            onDismissRequest()
                        } else {
                            Toast.makeText(context, "Por favor llena los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmar Cita", fontSize = 16.sp)
                }
            }
        }
    }
}