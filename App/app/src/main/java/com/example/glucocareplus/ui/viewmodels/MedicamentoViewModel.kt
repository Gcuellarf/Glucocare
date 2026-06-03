package com.example.glucocareplus.ui.viewmodels

import com.example.glucocareplus.data.local.MedicamentoDao
import com.example.glucocareplus.data.local.MedicamentoEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicamentoViewModel(private val medicamentoDao: MedicamentoDao) : ViewModel() {

    val listaMedicamentos: StateFlow<List<MedicamentoEntity>> = medicamentoDao.obtenerTodosLosMedicamentos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarMedicamento(nombre: String, dosis: String, horaProgramada: String) {
        viewModelScope.launch {
            val nuevo = MedicamentoEntity(
                nombre = nombre,
                dosis = dosis,
                horaProgramada = horaProgramada,
                estado = "PENDIENTE"
            )
            medicamentoDao.insertarMedicamento(nuevo)
        }
    }

    fun registrarToma(id: Int, horaActual: String) {
        viewModelScope.launch {
            medicamentoDao.actualizarEstadoToma(id, "TOMADO", horaActual)
        }
    }
}