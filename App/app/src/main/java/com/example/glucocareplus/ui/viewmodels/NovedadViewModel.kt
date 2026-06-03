package com.example.glucocareplus.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glucocareplus.data.local.NovedadDao
import com.example.glucocareplus.data.local.NovedadEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NovedadViewModel(private val novedadDao: NovedadDao) : ViewModel() {

    fun registrarNovedad(
        nauseas: Boolean, vomito: Boolean, cuestaRespirar: Boolean,
        dolorAbdomen: Boolean, muchoSueno: Boolean, debil: Boolean,
        dolorCabeza: Boolean, temblando: Boolean, palido: Boolean,
        sudando: Boolean, otroSintoma: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val horaActual = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val reporte = NovedadEntity(
                fecha = fechaActual,
                hora = horaActual,
                nauseas = nauseas,
                vomito = vomito,
                cuestaRespirar = cuestaRespirar,
                dolorAbdomen = dolorAbdomen,
                muchoSueno = muchoSueno,
                debil = debil,
                dolorCabeza = dolorCabeza,
                temblando = temblando,
                palido = palido,
                sudando = sudando,
                otroSintoma = otroSintoma
            )
            novedadDao.insertarNovedad(reporte)
            onSuccess()
        }
    }
}