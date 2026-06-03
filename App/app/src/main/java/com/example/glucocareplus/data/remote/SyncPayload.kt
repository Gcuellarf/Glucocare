package com.example.glucocareplus.data.remote

import com.example.glucocareplus.data.local.CitaEntity
import com.example.glucocareplus.data.local.GlucemiaEntity
import com.example.glucocareplus.data.local.MedicamentoEntity
import com.example.glucocareplus.data.local.NovedadEntity

data class SyncPayload(
    val username: String,
    val glucemias: List<GlucemiaEntity>,
    val citas: List<CitaEntity>,
    val medicamentos: List<MedicamentoEntity>,
    val novedades: List<NovedadEntity>
)

data class SyncResponse(
    val success: Boolean,
    val message: String
)