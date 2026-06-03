package com.example.glucocareplus.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glucocareplus.data.local.CitaDao
import com.example.glucocareplus.data.local.CitaEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CitaViewModel(private val citaDao: CitaDao) : ViewModel() {

    val todasLasCitas: StateFlow<List<CitaEntity>> = citaDao.obtenerTodasLasCitas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agendarCita(titulo: String, medico: String, lugar: String, hora: String, fechaIso: String) {
        viewModelScope.launch {
            val nuevaCita = CitaEntity(
                titulo = titulo,
                medico = medico,
                lugar = lugar,
                hora = hora,
                fechaIso = fechaIso
            )
            citaDao.insertarCita(nuevaCita)
        }
    }
}