package com.example.glucocareplus.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glucocareplus.data.local.*
import com.example.glucocareplus.data.remote.RetrofitClient
import com.example.glucocareplus.data.remote.SyncPayload
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SyncViewModel(
    private val glucemiaDao: GlucemiaDao,
    private val citaDao: CitaDao,
    private val medicamentoDao: MedicamentoDao,
    private val novedadDao: NovedadDao
) : ViewModel() {

    fun realizarSincronizacionCompleta(usernameActivo: String, onResultado: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val listaGlucemias = glucemiaDao.obtenerHistorialGlucemias().first()
                val listaCitas = citaDao.obtenerTodasLasCitas().first()
                val listaMedicamentos = medicamentoDao.obtenerTodosLosMedicamentos().first()
                val listaNovedades = novedadDao.obtenerHistorialNovedades().first()

                val payload = SyncPayload(
                    username = usernameActivo,
                    glucemias = listaGlucemias,
                    citas = listaCitas,
                    medicamentos = listaMedicamentos,
                    novedades = listaNovedades
                )

                // Enviamos a XAMPP por HTTP POST de forma asíncrona
                val respuesta = RetrofitClient.apiService.enviarDatosServidor(payload)
                if (respuesta.isSuccessful && respuesta.body()?.success == true) {
                    onResultado(true, respuesta.body()?.message ?: "Sincronizado")
                } else {
                    val codigoError = respuesta.code()
                    val cuerpoError = respuesta.errorBody()?.string() ?: ""
                    onResultado(false, "Error servidor ($codigoError): $cuerpoError")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResultado(false, "Detalle: ${e.localizedMessage}")
            }
        }
    }
}