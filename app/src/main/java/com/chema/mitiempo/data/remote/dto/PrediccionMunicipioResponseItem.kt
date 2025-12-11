package com.chema.mitiempo.data.remote.dto

data class PrediccionMunicipioResponseItem(
    val elaborado: String,
    val id: Int,
    val nombre: String,
    val origen: Origen,
    val prediccion: Prediccion,
    val provincia: String,
    val version: Double
)

data class Origen(
    val copyright: String,
    val enlace: String,
    val language: String,
    val notaLegal: String,
    val productor: String,
    val web: String
)

data class Prediccion(
    val dia: List<Dia>
)

data class Dia(
    val estadoCielo: List<EstadoCielo>,
    val fecha: String,
    val temperatura: Temperatura,
    val viento: List<Viento>
)

data class EstadoCielo(
    val descripcion: String,
    val periodo: String?,
    val value: String
)

data class Temperatura(
    val maxima: Int?,
    val minima: Int?
)

data class Viento(
    val direccion: String,
    val periodo: String?,
    val velocidad: Int
)