package com.example.glucocareplus.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glucocareplus.data.local.CitaEntity
import com.example.glucocareplus.ui.viewmodels.CitaViewModel
import java.util.Calendar

@Composable
fun CitasScreen(
    modifier: Modifier = Modifier,
    viewModel: CitaViewModel,
    innerPadding: PaddingValues
) {
    val citasAgendadas by viewModel.todasLasCitas.collectAsStateWithLifecycle()
    var mostrarSheet by remember { mutableStateOf(false) }

    // Obtenemos los datos de tiempo reales basados en el año corriente 2026
    val calendarioInstancia = remember { Calendar.getInstance() }
    val mesActual = calendarioInstancia.get(Calendar.MONTH) // 0 = Enero, 5 = Junio
    val anioActual = calendarioInstancia.get(Calendar.YEAR)  // 2026

    // Nombre estático descriptivo del mes
    val nombresMeses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val nombreMesAMostrar = nombresMeses[mesActual]

    // Calculamos los días totales del mes actual
    val maxDiasMes = calendarioInstancia.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Estado del día seleccionado por el usuario (Por defecto, arranca en el día actual)
    var diaSeleccionado by remember { mutableIntStateOf(calendarioInstancia.get(Calendar.DAY_OF_MONTH)) }

    // String ISO del día seleccionado para filtrar en la BD local: "YYYY-MM-DD"
    val fechaSeleccionadaIso = String.format("%04d-%02d-%02d", anioActual, mesActual + 1, diaSeleccionado)

    // Filtramos en caliente las citas que coinciden con el día seleccionado
    val citasDelDia = remember(citasAgendadas, fechaSeleccionadaIso) {
        citasAgendadas.filter { it.fechaIso == fechaSeleccionadaIso }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agendar cita", modifier = Modifier.size(34.dp))
            }
        }
    ) { localPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = localPadding.calculateBottomPadding())
        ) {
            // --- ENCABEZADO CON TU ESTILO EXACTO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .padding(start = 24.dp, end = 24.dp, top = innerPadding.calculateTopPadding() + 10.dp, bottom = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Mis Citas Médicas", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- NOMBRE DEL MES ---
            Text(
                text = "$nombreMesAMostrar de $anioActual",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // --- CALENDARIO MATRICIAL NATIVO ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Iniciales de los días de la semana
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("L", "M", "M", "J", "V", "S", "D").forEach { diaLetra ->
                            Text(diaLetra, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Rejilla de días del mes
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(maxDiasMes) { index ->
                            val diaIterado = index + 1
                            val esSeleccionado = diaIterado == diaSeleccionado

                            // Construimos el ISO de este día específico de la cuadrícula para ver si tiene eventos
                            val fechaCeldaIso = String.format("%04d-%02d-%02d", anioActual, mesActual + 1, diaIterado)
                            val tieneCitaAgendada = citasAgendadas.any { it.fechaIso == fechaCeldaIso }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = if (esSeleccionado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { diaSeleccionado = diaIterado },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = diaIterado.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                                        color = if (esSeleccionado) Color.White else Color.Black
                                    )
                                    // Punto indicador de cita programada (Semáforo de eventos)
                                    if (tieneCitaAgendada) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    color = if (esSeleccionado) Color.White else Color(0xFFE65100),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECCIÓN INFERIOR: DETALLES DE LAS CITAS ---
            Text(
                text = "Citas para el día ($diaSeleccionado de $nombreMesAMostrar)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (citasDelDia.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tienes citas médicas agendadas para este día.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(citasDelDia, key = { it.id }) { cita ->
                        FilaTarjetaCita(cita = cita)
                    }
                }
            }
        }

        // --- HOJA DESPLEGABLE DE REGISTRO ---
        CitaRegisterSheet(
            showSheet = mostrarSheet,
            fechaSeleccionadaIso = fechaSeleccionadaIso,
            onDismissRequest = { mostrarSheet = false },
            onGuardarCita = { titulo, medico, lugar, hora ->
                viewModel.agendarCita(titulo, medico, lugar, hora, fechaSeleccionadaIso)
            }
        )
    }
}

// Tarjeta individual para listar cada cita médica médica
@Composable
fun FilaTarjetaCita(cita: CitaEntity) {
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
            Column(modifier = Modifier.weight(0.7f)) {
                Text(text = cita.titulo, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (cita.medico.isNotBlank()) {
                    Text(text = "Médico: ${cita.medico}", fontSize = 14.sp, color = Color.DarkGray)
                }
                Text(text = "Lugar: ${cita.lugar}", fontSize = 13.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = cita.hora, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}