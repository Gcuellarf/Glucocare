package com.example.glucocareplus.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glucocareplus.data.local.GlucemiaDao
import com.example.glucocareplus.data.local.GlucemiaEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class GlucemiaViewModel(private val glucemiaDao: GlucemiaDao) : ViewModel() {

    val historialGlucemias: StateFlow<List<GlucemiaEntity>> = glucemiaDao.obtenerHistorialGlucemias()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarGlucemia(valor: Int, momento: String, estadoSemaforo: String) {
        viewModelScope.launch {
            val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val horaActual = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val nuevoRegistro = GlucemiaEntity(
                valor = valor,
                momento = momento,
                fecha = fechaActual,
                hora = horaActual,
                estadoSemaforo = estadoSemaforo
            )
            glucemiaDao.insertarGlucemia(nuevoRegistro)
        }
    }
}