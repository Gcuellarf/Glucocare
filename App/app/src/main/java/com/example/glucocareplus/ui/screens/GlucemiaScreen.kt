package com.example.glucocareplus.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glucocareplus.data.local.GlucemiaEntity
import com.example.glucocareplus.ui.viewmodels.GlucemiaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlucemiaScreen(
    modifier: Modifier = Modifier,
    viewModel: GlucemiaViewModel,
    innerPadding: PaddingValues // Recibimos el padding del MainActivity Scaffold
) {
    val historial by viewModel.historialGlucemias.collectAsStateWithLifecycle()
    var mostrarSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir registro glucemia", modifier = Modifier.size(34.dp))
            }
        }
    ) { localPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .fillMaxSize()
                .padding(bottom = localPadding.calculateBottomPadding())
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = innerPadding.calculateTopPadding() + 10.dp,
                        bottom = 20.dp
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Mis Glucometrías", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN SUPERIOR: GRÁFICO LINEAL NATIVO ---
            Text(
                text = "Tendencia Reciente (mg/dL)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                if (historial.size < 2) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Registra al menos 2 valores para ver la tendencia.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    // Tomamos los últimos 7 registros y los invertimos para que se lean cronológicamente de izquierda a derecha
                    val datosGrafico = historial.take(7).reversed()
                    GraficoLinealGlucemia(registros = datosGrafico)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECCIÓN INFERIOR: TABLA DE HISTORIAL ---
            Text(
                text = "Historial de Registros",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (historial.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay glucometrías registradas el día de hoy.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historial, key = { it.id }) { registro ->
                        FilaHistorialGlucemia(registro = registro)
                    }
                }
            }
        }

        // --- HOJA DESPLEGABLE DE REGISTRO ---
        GlucemiaRegisterSheet(
            showSheet = mostrarSheet,
            onDismissRequest = { mostrarSheet = false },
            onGuardarRegistro = { valor, momento, estadoSemaforo ->
                viewModel.agregarGlucemia(valor, momento, estadoSemaforo)
            }
        )
    }
}

// --- COMPONENTE CANVAS PARA EL DIBUJO DEL GRÁFICO LINEAL ---
@Composable
fun GraficoLinealGlucemia(registros: List<GlucemiaEntity>) {
    val colorLinea = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
    ) {
        val anchoMax = size.width
        val altoMax = size.height

        // Puntos en el eje X espaciados uniformemente
        val espacioX = anchoMax / (registros.size - 1)

        // Normalización del eje Y (Rangos lógicos de glucemia: 40 mg/dL mínimo, 250 mg/dL máximo en la gráfica)
        val glucemiaMin = 40f
        val glucemiaMax = 250f
        val rangoY = glucemiaMax - glucemiaMin

        val puntosPintar = registros.mapIndexed { index, item ->
            val x = index * espacioX
            // En Canvas, la coordenada Y = 0 empieza arriba, por eso restamos del altoMax
            val porcentajeY = (item.valor - glucemiaMin) / rangoY
            val y = altoMax - (porcentajeY.coerceIn(0f, 1f) * altoMax)
            Offset(x, y)
        }

        // 1. Dibujar líneas de cuadrícula guía de fondo
        val lineaGuiaNormal = altoMax - (((100f - glucemiaMin) / rangoY) * altoMax)
        drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(0f, lineaGuiaNormal), Offset(anchoMax, lineaGuiaNormal), strokeWidth = 2f)

        // 2. Trazar el camino de la curva lineal
        val path = Path().apply {
            puntosPintar.forEachIndexed { index, offset ->
                if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
            }
        }
        drawPath(path = path, color = colorLinea, style = Stroke(width = 6f))

        // 3. Dibujar los nodos (círculos) en cada medición e insertar el número encima
        puntosPintar.forEachIndexed { index, offset ->
            drawCircle(color = colorLinea, radius = 5.dp.toPx(), center = offset)
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = offset)
        }
    }
}

// --- COMPONENTE: TARJETA INDIVIDUAL PARA CADA REGISTRO DE LA TABLA ---
@Composable
fun FilaHistorialGlucemia(registro: GlucemiaEntity) {
    val (colorSemaforo, textoSemaforo) = when (registro.estadoSemaforo) {
        "HIPO" -> Pair(Color(0xFFC5221F), "Hipoglucemia")
        "NORMAL" -> Pair(Color(0xFF137333), "Rango Objetivo")
        "HIPER" -> Pair(Color(0xFFB06000), "Hiperglucemia")
        else -> Pair(Color.Gray, "Sin estado")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicador tipo semáforo (Punto circular notable de estado)
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(colorSemaforo, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${registro.valor} mg/dL",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorSemaforo
                    )
                    Text(
                        text = if (registro.momento == "ANTES") "Antes de comer" else "Después de comer",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = registro.hora, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(text = registro.fecha, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}