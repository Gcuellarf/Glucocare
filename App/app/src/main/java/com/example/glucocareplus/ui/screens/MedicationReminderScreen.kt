package com.example.glucocareplus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glucocareplus.data.local.MedicamentoEntity
import com.example.glucocareplus.ui.viewmodels.MedicamentoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationReminderScreen(
    modifier: Modifier = Modifier,
    viewModel: MedicamentoViewModel,
    onNavigateToNovedades: () -> Unit
) {
    val context = LocalContext.current
    val listaMedicamentos by viewModel.listaMedicamentos.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var nombreMed by remember { mutableStateOf("") }
    var dosisMed by remember { mutableStateOf("") }
    var horaMed by remember { mutableStateOf("Seleccionar hora") }
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true }, // Abrimos el formulario
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(34.dp))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .padding(start = 24.dp, end = 24.dp, top = innerPadding.calculateTopPadding() + 10.dp, bottom = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Mis Medicamentos", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (listaMedicamentos.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tienes medicamentos programados.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(listaMedicamentos, key = { it.id }) { medicamento ->
                        val estadoCalculado = evaluarEstadoReal(medicamento.estado, medicamento.horaProgramada)
                        val medicamentoEvaluado = medicamento.copy(estado = estadoCalculado)

                        CardMedicamento(
                            medicamento = medicamentoEvaluado,
                            onConfirmarToma = { tardio ->
                                val horaActual = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                viewModel.registrarToma(medicamento.id, if (tardio) "${medicamento.horaProgramada} (Tardío)" else horaActual)
                            },
                            onReportarMalestar = onNavigateToNovedades
                        )
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nueva Medicación", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = nombreMed,
                        onValueChange = { nombreMed = it },
                        label = { Text("Nombre del medicamento") },
                        placeholder = { Text("Ej: Metformina") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dosisMed,
                        onValueChange = { dosisMed = it },
                        label = { Text("Dosis") },
                        placeholder = { Text("Ej: 850 mg / 1 tableta") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = horaMed)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (nombreMed.isNotBlank() && dosisMed.isNotBlank() && horaMed != "Seleccionar hora") {
                                viewModel.agregarMedicamento(nombreMed, dosisMed, horaMed)
                                // Limpiar y cerrar
                                nombreMed = ""
                                dosisMed = ""
                                horaMed = "Seleccionar hora"
                                showSheet = false
                                Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Medicamento", fontSize = 16.sp)
                    }
                }
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                onConfirm = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    horaMed = formatter.format(cal.time)
                    showTimePicker = false
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        },
        text = { content() }
    )
}

@Composable
fun CardMedicamento(
    medicamento: MedicamentoEntity,
    onConfirmarToma: (Boolean) -> Unit,
    onReportarMalestar: () -> Unit
) {
    val (backgroundColor, borderColor, statusText) = when (medicamento.estado) {
        "TOMADO" -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), "Tomado a las ${medicamento.horaTomada}")
        "NO_REGISTRADO" -> Triple(Color(0xFFFEF7E0), Color(0xFFB06000), "No registrado / Pasado de hora")
        else -> Triple(Color.White, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), "Pendiente") // PENDIENTE
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = medicamento.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Dosis: ${medicamento.dosis}", fontSize = 15.sp, color = Color.Gray)
                }
                Text(
                    text = medicamento.horaProgramada,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = borderColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = borderColor
                )

                when (medicamento.estado) {
                    "PENDIENTE" -> {
                        Button(
                            onClick = { onConfirmarToma(false) },
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6600CC))
                        ) {
                            Text("Tomar")
                        }
                    }
                    "NO_REGISTRADO" -> {
                        Column(horizontalAlignment = Alignment.End) {
                            Button(
                                onClick = { onConfirmarToma(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB06000)),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Sí, me la tomé", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = onReportarMalestar,
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("¿Te sientes mal?", color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    }
                    "TOMADO" -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Confirmado",
                            tint = Color(0xFF137333),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun evaluarEstadoReal(estadoOriginal: String, horaProgramada: String): String {
    if (estadoOriginal != "PENDIENTE") return estadoOriginal

    return try {
        // Formato con el que guardas las horas (Ej: "07:00 AM")
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Convertimos la hora programada a un objeto Date (usa el día de hoy por defecto)
        val horaProgDate = sdf.parse(horaProgramada) ?: return estadoOriginal

        // Obtenemos solo la hora y minutos del momento actual
        val horaActualStr = sdf.format(Date())
        val horaActualDate = sdf.parse(horaActualStr) ?: return estadoOriginal

        // DEFINIR TOLERANCIA: 60 minutos en milisegundos (60 * 60 * 1000)
        val margenToleranciaMilis = 60 * 60 * 1000

        // Si la hora actual pasó de la programada + la tolerancia
        if (horaActualDate.time > (horaProgDate.time + margenToleranciaMilis)) {
            "NO_REGISTRADO"
        } else {
            "PENDIENTE"
        }
    } catch (e: Exception) {
        estadoOriginal // En caso de un error de formato, mantiene el original de la DB
    }
}