package com.amaurypm.musicplayerdiplo.data.local.model

import android.content.Context
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log

class AudioRepository(private val context: Context) {

    fun getAllAudio(): List<MusicFile>{

        val tempAudioList = mutableListOf<MusicFile>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //Para que solamente se obtengan contenidos de música
        //Sí es 1, es un archivo de música
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} == 1"

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
            while(cursor.moveToNext()){
                val album = cursor.getString(0)
                val title = cursor.getString(1)
                val duration = cursor.getString(2)
                val path = cursor.getString(3)
                val artist = cursor.getString(4)

                val musicFile = MusicFile(path, title, artist, album, duration ?: "0")

                Log.d("MUSICA", "Path: $path - Album: $album")

                tempAudioList.add(musicFile)
            }
            cursor.close()
        }

        return tempAudioList
    }

}