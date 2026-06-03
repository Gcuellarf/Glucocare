package com.example.glucocareplus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.glucocareplus.ui.viewmodels.NovedadViewModel

@Composable
fun NovedadScreen(
    modifier: Modifier = Modifier,
    viewModel: NovedadViewModel,
    innerPadding: PaddingValues,
    onReporteExitoso: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados de las perillas (Switches)
    var nauseas by remember { mutableStateOf(false) }
    var vomito by remember { mutableStateOf(false) }
    var cuestaRespirar by remember { mutableStateOf(false) }
    var dolorAbdomen by remember { mutableStateOf(false) }
    var muchoSueno by remember { mutableStateOf(false) }
    var debil by remember { mutableStateOf(false) }
    var dolorCabeza by remember { mutableStateOf(false) }
    var temblando by remember { mutableStateOf(false) }
    var palido by remember { mutableStateOf(false) }
    var sudando by remember { mutableStateOf(false) }
    var otroSintoma by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {

        // --- ENCABEZADO CON TU ESTILO EXACTO ---
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
            Text("¿Algo anda mal?", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // --- CUERPO DEL FORMULARIO (SCROLEABLE) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Selecciona los síntomas que presentas en este momento:",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Fila de interruptores
            FilaSintomaSwitch(label = "Náuseas", checked = nauseas, onCheckedChange = { nauseas = it })
            FilaSintomaSwitch(label = "Vómito", checked = vomito, onCheckedChange = { vomito = it })
            FilaSintomaSwitch(label = "Me cuesta respirar", checked = cuestaRespirar, onCheckedChange = { cuestaRespirar = it })
            FilaSintomaSwitch(label = "Me duele el abdomen", checked = dolorAbdomen, onCheckedChange = { dolorAbdomen = it })
            FilaSintomaSwitch(label = "Me da mucho sueño", checked = muchoSueno, onCheckedChange = { muchoSueno = it })
            FilaSintomaSwitch(label = "Me siento débil", checked = debil, onCheckedChange = { debil = it })
            FilaSintomaSwitch(label = "Me duele mucho la cabeza", checked = dolorCabeza, onCheckedChange = { dolorCabeza = it })
            FilaSintomaSwitch(label = "Estoy temblando", checked = temblando, onCheckedChange = { temblando = it })
            FilaSintomaSwitch(label = "Estoy pálido/a", checked = palido, onCheckedChange = { palido = it })
            FilaSintomaSwitch(label = "Estoy sudando", checked = sudando, onCheckedChange = { sudando = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto abierto para otros síntomas
            OutlinedTextField(
                value = otroSintoma,
                onValueChange = { otroSintoma = it },
                label = { Text("Otro síntoma o malestar") },
                placeholder = { Text("Ej: Visión borrosa, boca seca...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Envío
            Button(
                onClick = {
                    viewModel.registrarNovedad(
                        nauseas, vomito, cuestaRespirar, dolorAbdomen, muchoSueno,
                        debil, dolorCabeza, temblando, palido, sudando, otroSintoma
                    ) {
                        Toast.makeText(context, "Reporte guardado correctamente", Toast.LENGTH_LONG).show()
                        // Reseteamos el formulario
                        nauseas = false; vomito = false; cuestaRespirar = false; dolorAbdomen = false
                        muchoSueno = false; debil = false; dolorCabeza = false; temblando = false
                        palido = false; sudando = false; otroSintoma = ""
                        onReporteExitoso()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5221F)) // Color rojo clínico de alerta
            ) {
                Text("Registrar Síntomas", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Componente reutilizable para cada síntoma
@Composable
fun FilaSintomaSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}