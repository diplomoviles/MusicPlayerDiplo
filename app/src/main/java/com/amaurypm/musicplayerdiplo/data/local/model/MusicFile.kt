package com.amaurypm.musicplayerdiplo.data.local.model

data class MusicFile(
    val data: String, //Aquí viene la ruta del archivo
    val title: String,
    val artist: String,
    val album: String,
    val duration: String //Duración en milisegundos
)
