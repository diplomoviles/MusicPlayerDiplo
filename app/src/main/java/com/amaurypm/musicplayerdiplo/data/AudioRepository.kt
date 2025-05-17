package com.amaurypm.musicplayerdiplo.data

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile

class AudioRepository(
    private val context: Context
) {

    fun getAllAudio(): MutableList<MusicFile>{
        val tempAudioList = mutableListOf<MusicFile>()

        //Definimos elementos para nuestra query al sistema de archivos externos

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} == 1"  //Condición para seleccionar archivos de música

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

        if(cursor != null){
            //Recorremos el cursor si hay algún archivo de audio
            while(cursor.moveToNext()){
                val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))

                val musicFile = MusicFile(data, title, artist, album, duration)

                Log.d("MUSICA", "Ruta: $data - Album: $album")

                tempAudioList.add(musicFile)
            }
            cursor.close()
        }

        return tempAudioList
    }

}