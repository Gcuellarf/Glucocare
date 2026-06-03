package com.example.glucocareplus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlucemiaRegisterSheet(
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    onGuardarRegistro: (valor: Int, momento: String, estadoSemaforo: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Estados del formulario
    var valorGlucemiaStr by remember { mutableStateOf("") }
    var momentoSeleccionado by remember { mutableStateOf("") } // "ANTES" o "DESPUES"

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
                Text("Registrar Glucometría", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // --- 1. GUÍA VISUAL: EL GLUCÓMETRO DIDÁCTICO ---
                GuiaVisualGlucometro(valorMostrar = valorGlucemiaStr)

                Spacer(modifier = Modifier.height(20.dp))

                // --- 2. ENTRADA OBLIGATORIA DEL MOMENTO (Antes/Después de comer) ---
                Text(
                    text = "Momento de la medición *",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val esAntes = momentoSeleccionado == "ANTES"
                    val esDespues = momentoSeleccionado == "DESPUES"

                    FilterChip(
                        selected = esAntes,
                        onClick = { momentoSeleccionado = "ANTES" },
                        label = { Text("Antes de comer", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        leadingIcon = if (esAntes) { { Icon(Icons.Default.Check, null) } } else null
                    )
                    FilterChip(
                        selected = esDespues,
                        onClick = { momentoSeleccionado = "DESPUES" },
                        label = { Text("Después de comer (2h)", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        leadingIcon = if (esDespues) { { Icon(Icons.Default.Check, null) } } else null
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3. INPUT NUMÉRICO DEL VALOR ---
                OutlinedTextField(
                    value = valorGlucemiaStr,
                    onValueChange = { input ->
                        // Filtro para aceptar solo números enteros de hasta 3 dígitos
                        if (input.all { it.isDigit() } && input.length <= 3) {
                            valorGlucemiaStr = input
                        }
                    },
                    label = { Text("Nivel de azúcar (mg/dL) *") },
                    placeholder = { Text("Ej: 105") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- 4. EL SEMÁFORO NOTABLE (Dinámico) ---
                val valorInt = valorGlucemiaStr.toIntOrNull()
                val estadoSemaforo = calcularSemaforo(valorInt, momentoSeleccionado)

                SemaforoVisualCard(estado = estadoSemaforo)

                Spacer(modifier = Modifier.height(24.dp))

                // --- 5. BOTÓN DE GUARDAR ---
                Button(
                    onClick = {
                        val valor = valorGlucemiaStr.toIntOrNull()
                        if (valor != null && momentoSeleccionado.isNotBlank()) {
                            onGuardarRegistro(valor, momentoSeleccionado, estadoSemaforo)
                            // Limpiar estados y cerrar
                            valorGlucemiaStr = ""
                            momentoSeleccionado = ""
                            onDismissRequest()
                        } else {
                            Toast.makeText(context, "Por favor, completa los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Registro", fontSize = 16.sp)
                }
            }
        }
    }
}

// --- COMPONENTE: DIBUJO ESTRUCTURAL DEL GLUCÓMETRO ---
@Composable
fun GuiaVisualGlucometro(valorMostrar: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        modifier = Modifier.width(160.dp).padding(8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            // Tira reactiva superior
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(30.dp)
                    .background(Color.LightGray, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
            // Cuerpo del glucómetro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Pantalla digital interna
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.65f)
                        .background(Color(0xFFE0F7FA), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = valorMostrar.ifEmpty { "--" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006064)
                    )
                }
            }
        }
    }
}

// --- COMPONENTE: TARJETA DEL SEMÁFORO EXTREMADAMENTE NOTABLE ---
@Composable
fun SemaforoVisualCard(estado: String) {
    val (bgColor, textColor, titulo, descripcion) = when (estado) {
        "HIPO" -> quadruplet(Color(0xFFFCE8E6), Color(0xFFC5221F), "🔴 HIPOGLUCEMIA", "Nivel peligrosamente bajo. Consume carbohidratos rápidos.")
        "NORMAL" -> quadruplet(Color(0xE2E6F4EA), Color(0xFF137333), "🟢 RANGO OBJETIVO", "¡Excelente! Tu nivel de azúcar está controlado en un rango óptimo.")
        "HIPER" -> quadruplet(Color(0xFFFEF7E0), Color(0xFFB06000), "🟡 HIPERGLUCEMIA", "Nivel elevado. Revisa tu medicación, hidratación y plan alimentario.")
        else -> quadruplet(Color(0xFFF5F5F5), Color.Gray, "⚪ Esperando Datos", "Ingresa el momento y el valor para analizar el estado.")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, textColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
            Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = descripcion, fontSize = 13.sp, color = Color.DarkGray)
        }
    }
}

// Función auxiliar lógica pura para definir los rangos médicos según la ADA
fun calcularSemaforo(valor: Int?, momento: String): String {
    if (valor == null || momento.isBlank()) return "NINGUNO"

    return if (momento == "ANTES") {
        when {
            valor < 70 -> "HIPO"
            valor in 70..130 -> "NORMAL"
            else -> "HIPER"
        }
    } else { // DESPUES DE COMER
        when {
            valor < 70 -> "HIPO"
            valor <= 180 -> "NORMAL"
            else -> "HIPER"
        }
    }
}

// Estructura de datos temporal para legibilidad del Semáforo
data class Quadruplet<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
fun <A, B, C, D> quadruplet(a: A, b: B, c: C, d: D): Quadruplet<A, B, C, D> = Quadruplet(a, b, c, d)