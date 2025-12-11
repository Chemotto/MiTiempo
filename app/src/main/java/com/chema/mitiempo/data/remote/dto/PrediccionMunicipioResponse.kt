package com.chema.mitiempo.data.remote.dto

data class PrediccionMunicipioResponse(
    val descripcion: String,
    val estado: Int,
    val datos: String,
    val metadatos: String
)