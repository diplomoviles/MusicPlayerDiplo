package com.amaurypm.musicplayerdiplo.ui.adapters

import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import com.amaurypm.musicplayerdiplo.databinding.MusicItemBinding
import com.bumptech.glide.Glide

class SongsViewHolder(
    private val binding: MusicItemBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(musicFile: MusicFile){

        binding.tvSongName.text = musicFile.title

        var image: ByteArray? = null

        try{

            image = getAlbumArt(musicFile.data)

        }catch(e: Exception){

        }

        Glide.with(binding.root.context)
            .load(image)
            .error(R.drawable.ic_song)
            .into(binding.ivSongImage)

    }

    //Para obtener la imagen del archivo de música (si existe)
    private fun getAlbumArt(uri: String): ByteArray?{

        val retriever = MediaMetadataRetriever()

        retriever.setDataSource(uri)

        val art: ByteArray? = retriever.embeddedPicture

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            retriever.close()
        else
            retriever.release()

        return art
    }

}