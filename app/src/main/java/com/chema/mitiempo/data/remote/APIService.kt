package com.chema.mitiempo.data.remote

import com.chema.mitiempo.data.remote.dto.PrediccionMunicipioResponse
import com.chema.mitiempo.data.remote.dto.PrediccionMunicipioResponseItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface APIService {
    // Llamada 1: Le pasamos el código de municipio y la API Key en la cabecera
    @GET("prediccion/especifica/municipio/diaria/{codigoPoblacion}")
    suspend fun getUrlAemet(
        @Path("codigoPoblacion") codigo: String,
        @Header("api_key") apiKey: String
    ): Response<PrediccionMunicipioResponse> // Devuelve la clase que generamos

    // Llamada 2: Usamos una URL dinámica
    @GET
    suspend fun getPrediccion(@Url url: String): Response<List<PrediccionMunicipioResponseItem>> // Devuelve una lista de la otra clase generada
}