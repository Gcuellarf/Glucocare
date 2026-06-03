package com.example.glucocareplus.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glucocareplus.ui.viewmodels.CitaViewModel
import com.example.glucocareplus.ui.viewmodels.GlucemiaViewModel
import com.example.glucocareplus.ui.viewmodels.SyncViewModel // <-- Importamos tu nuevo SyncViewModel

@Composable
fun InicioScreen(
    modifier: Modifier = Modifier,
    usernameActivo: String,               // <-- Recibimos el usuario actual para la BD en la nube
    glucemiaViewModel: GlucemiaViewModel,
    citaViewModel: CitaViewModel,
    syncViewModel: SyncViewModel,         // <-- Inyectamos el cargador de sincronización
    innerPadding: PaddingValues,
    onNavegarA: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Estado local para controlar si la nube está girando en plena sincronización
    var estaSincronizando by remember { mutableStateOf(false) }

    // Animación de rotación continua para el ícono de la nube mientras sincroniza
    val rotacionInfinita by rememberInfiniteTransition(label = "SyncRotation").animateFloat(
        initialValue = 0f,
        targetValue = if (estaSincronizando) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudRotation"
    )

    val historialGlucemias by glucemiaViewModel.historialGlucemias.collectAsStateWithLifecycle()
    val todasLasCitas by citaViewModel.todasLasCitas.collectAsStateWithLifecycle()

    val ultimaGlucemia = historialGlucemias.firstOrNull()
    val proximaCita = todasLasCitas.firstOrNull()

    Column(modifier = modifier.fillMaxSize()) {

        // --- ENCABEZADO CORPORATIVO CON BOTÓN DE NUBE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primary)
                .padding(
                    start = 24.dp,
                    end = 16.dp, // Reducido un poco para dar espacio cómodo al botón
                    top = innerPadding.calculateTopPadding() + 10.dp,
                    bottom = 24.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Panel de Control", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Tu salud bajo control paso a paso", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }

                // --- BOTÓN DE SINCRONIZACIÓN CLOUD ---
                IconButton(
                    onClick = {
                        if (!estaSincronizando) {
                            estaSincronizando = true
                            syncViewModel.realizarSincronizacionCompleta(usernameActivo) { exito, mensaje ->
                                estaSincronizando = false
                                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !estaSincronizando
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sincronizar con XAMPP",
                        tint = if (estaSincronizando) Color.White.copy(alpha = 0.6f) else Color.White,
                        modifier = Modifier
                            .size(34.dp)
                            .rotate(rotacionInfinita) // Aplica el giro si está activo
                    )
                }
            }
        }

        // --- CUERPO SCROLEABLE ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // --- SECCIÓN 1: ESTADO ACTUAL (TARJETAS DINÁMICAS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardResumenInicio(
                    modifier = Modifier.weight(1f),
                    titulo = "Último Registro",
                    valor = if (ultimaGlucemia != null) "${ultimaGlucemia.valor} mg/dL" else "--",
                    subtexto = if (ultimaGlucemia != null) ultimaGlucemia.fecha else "Sin registros",
                    icono = Icons.Default.Bloodtype,
                    colorIcono = Color(0xFFC5221F)
                )

                CardResumenInicio(
                    modifier = Modifier.weight(1f),
                    titulo = "Próxima Cita",
                    valor = proximaCita?.titulo ?: "Ninguna",
                    subtexto = proximaCita?.hora ?: "Agenda libre",
                    icono = Icons.Default.CalendarMonth,
                    colorIcono = MaterialTheme.colorScheme.primary
                )
            }

            // --- SECCIÓN 2: ACCESOS RÁPIDOS DE ACCIÓN ---
            Text("Accesos Rápidos", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotónAccesoRapido(
                    modifier = Modifier.weight(1f),
                    texto = "Glucemia",
                    icono = Icons.Default.Add,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onNavegarA("glucemias") }
                )

                BotónAccesoRapido(
                    modifier = Modifier.weight(1f),
                    texto = "Novedad",
                    icono = Icons.Default.Warning,
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC5221F),
                    onClick = { onNavegarA("formulario") }
                )
            }

            // --- SECCIÓN 3: COMPROMISOS / NOTAS DEL DÍA ---
            Text("Recordatorio Diario", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Tratamiento",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "Control de Medicamentos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No olvides revisar los horarios de tus dosis en la barra inferior para mantener tu estabilidad.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// (Los sub-componentes CardResumenInicio y BotónAccesoRapido se mantienen exactamente igual abajo...)
@Composable
fun CardResumenInicio(modifier: Modifier = Modifier, titulo: String, valor: String, subtexto: String, icono: ImageVector, colorIcono: Color) {
    Card(modifier = modifier.height(125.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(titulo, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Icon(imageVector = icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtexto, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1)
            }
        }
    }
}

@Composable
fun BotónAccesoRapido(modifier: Modifier = Modifier, texto: String, icono: ImageVector, containerColor: Color, contentColor: Color, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor), contentPadding = PaddingValues(horizontal = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}