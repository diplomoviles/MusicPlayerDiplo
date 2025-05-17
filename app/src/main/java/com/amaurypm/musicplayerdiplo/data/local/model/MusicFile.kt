package com.amaurypm.musicplayerdiplo.data.local.model

data class MusicFile(
    val data: String,  //Aquí va a venir la ruta del archivo (uri)
    val title: String,
    val artist: String,
    val album: String,
    val duration: String
)
