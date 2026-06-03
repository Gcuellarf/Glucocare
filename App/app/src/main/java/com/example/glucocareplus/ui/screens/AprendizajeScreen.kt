package com.example.glucocareplus.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AprendizajeScreen(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues
) {
    val scrollState = rememberScrollState()

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
            Text("Aula de Aprendizaje", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // --- CONTENIDO EDUCATIVO (SCROLEABLE) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Conoce las pautas clave para un control exitoso de tu salud diaria.",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp) // <-- Cambiado aquí
            )

            // --- SECCIÓN 1: ¿QUÉ ES? ---
            TarjetaExpandibleEducativa(
                titulo = "¿Qué es la Diabetes?",
                contenido = "Es una condición crónica donde el cuerpo tiene dificultades para procesar la glucosa (azúcar) en la sangre. " +
                        "Esto ocurre porque el páncreas no produce suficiente insulina (Tipo 1) o porque las células no responden " +
                        "correctamente a ella (Tipo 2). La insulina es la 'llave' que permite al azúcar entrar a tus células para darte energía."
            )

            // --- SECCIÓN 2: ALIMENTACIÓN ---
            TarjetaExpandibleEducativa(
                titulo = "Alimentación Inteligente",
                contenido = "Controlar los carbohidratos es fundamental. Prioriza carbohidratos complejos (integrales, verduras y legumbres) " +
                        "en lugar de azúcares simples o refinados.\n\n" +
                        "• Método del plato: Llena la mitad con verduras, un cuarto con proteínas magras (pollo, pescado, huevo) y el cuarto restante con carbohidratos.\n" +
                        "• Evita bebidas azucaradas, jugos industriales y ultraprocesados."
            )

            // --- SECCIÓN 3: EJERCICIO ---
            TarjetaExpandibleEducativa(
                titulo = "El Ejercicio como Medicina",
                contenido = "La actividad física ayuda a tus músculos a absorber el azúcar en la sangre de manera natural, reduciendo la necesidad " +
                        "de insulina extra.\n\n" +
                        "• Recomendación: Al menos 150 minutos a la semana de ejercicio moderado (caminar a paso rápido, montar en bicicleta).\n" +
                        "• Precaución: Revisa tu glucemia antes de empezar. Si está por debajo de 100 mg/dL, consume un pequeño snack para evitar hipoglucemias."
            )

            // --- SECCIÓN 4: PASO A PASO INSULINA ---
            TarjetaExpandibleEducativa(
                titulo = "¿Cómo me aplico la insulina?",
                contenido = "1. Limpieza: Lava tus manos y limpia la zona de inyección con alcohol.\n" +
                        "2. Carga: Si usas lapicero, purga 2 unidades para quitar burbujas de aire y luego selecciona tu dosis exacta.\n" +
                        "3. Inyección: Pellizca suavemente la piel e inserta la aguja en un ángulo de 90 grados. Presiona el botón a fondo.\n" +
                        "4. Espera: Cuenta 10 segundos antes de retirar la aguja para asegurar que no haya fugas.\n" +
                        "5. Rotación: Varía los sitios (abdomen, muslos, brazos) para evitar endurecimientos en la piel (lipohipertrofia)."
            )

            // --- SECCIÓN 5: PASO A PASO GLUCOMETRÍA ---
            TarjetaExpandibleEducativa(
                titulo = "¿Cómo me tomo la glucometría?",
                contenido = "1. Manos limpias: Lávate con agua y jabón. El alcohol puede alterar el resultado si no se ha secado por completo.\n" +
                        "2. Preparación: Inserta la tira reactiva en el glucómetro apagado; este encenderá automáticamente.\n" +
                        "3. El pinchazo: Usa el lancetero en los laterales de las yemas de los dedos (duele mucho menos que en el centro).\n" +
                        "4. Medición: Coloca la gota de sangre en el borde de la tira reactiva y espera el conteo en pantalla.\n" +
                        "5. Registro: No olvides anotar el valor y especificar si fue antes o después de comer en la sección 'Glucemias' de tu app."
            )

            // --- SECCIÓN 6: AGREGADA POR RESPALDO: SÍNTOMAS DE ALERTA ---
            TarjetaExpandibleEducativa(
                titulo = "Señales de Alerta: ¿Cuándo actuar?",
                contenido = "• Bajón (Hipoglucemia < 70 mg/dL): Sudor frío, temblores, hambre feroz, mareo. Aplica la regla de los 15g: toma 1/2 vaso de jugo o agua con azúcar y reevalúa en 15 minutos.\n\n" +
                        "• Subidón (Hiperglucemia > 250 mg/dL): Sed excesiva, ganas frecuentes de orinar, náuseas, dolor abdominal o dificultad para respirar. Si presentas estos últimos, usa el formulario de novedades y consulta urgencias."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- COMPONENTE INTERNO: TARJETA CON ANIMACIÓN DE EXPANSIÓN ---
@Composable
fun TarjetaExpandibleEducativa(
    titulo: String,
    contenido: String
) {
    var expandida by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandida = !expandida },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expandida) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color(0xFFF9F9F9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(0.9f)
                )
                Icon(
                    imageVector = if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expandida) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expandida) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = contenido,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}