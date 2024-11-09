package com.amaurypm.musicplayerdiplo.data

import android.content.Context
import android.provider.MediaStore
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile

class AudioRepository(
    private val context: Context
) {

    fun getAllAudio(): MutableList<MusicFile>{
        //Aquí leemos todos los archivos de audio del dispositivo en almacenamiento externo

        val tempAudioList = mutableListOf<MusicFile>()

        //Generamos los elementos que me pide el query
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_MUSIC}"  //Filtro o condición para que el query solamente me dé archivos de música

        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            null
        )

        return tempAudioList
    }

}