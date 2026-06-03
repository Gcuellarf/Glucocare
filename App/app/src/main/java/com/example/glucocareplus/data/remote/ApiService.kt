package com.example.glucocareplus.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("glucocareplus/api/sync.php")
    suspend fun enviarDatosServidor(@Body payload: SyncPayload): Response<SyncResponse>
}