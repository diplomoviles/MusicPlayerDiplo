package com.amaurypm.musicplayerdiplo.ui.adapters

import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import com.amaurypm.musicplayerdiplo.databinding.MusicItemBinding
import com.bumptech.glide.Glide
import java.lang.Exception

class SongViewHolder(
    private val binding: MusicItemBinding,
    private val onSongClick: (Int) -> Unit //Queremos solamente la posición en este caso
): RecyclerView.ViewHolder(binding.root) {

    private var currentPositionItem: Int? = null

    init {
        binding.root.setOnClickListener {
            currentPositionItem?.let(onSongClick)
        }
    }

    fun bind(song: MusicFile, position: Int){
        currentPositionItem = position

        binding.tvSongName.text = song.title

        var image: ByteArray? = null
        try{
            image = getAlbumArt(song.data)
        }catch (e: Exception){
            //Manejamos la excepción
            e.printStackTrace()
        }

        Glide.with(binding.root.context).asBitmap()
            .load(image)
            .error(R.drawable.ic_song)
            .into(binding.ivSongImage)
    }

    companion object{
        fun create(
            parent: ViewGroup,
            onSongClick: (Int) -> Unit
        ): SongViewHolder{
            val binding = MusicItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SongViewHolder(binding, onSongClick)
        }
    }


    //Para recuperar la imagen los metadatos del mp3
    private fun getAlbumArt(uri: String): ByteArray?{
        val retriever = MediaMetadataRetriever()

        retriever.setDataSource(uri)

        val art: ByteArray? = retriever.embeddedPicture

        //Liberamos el retriever dependiendo de la versión
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            retriever.close()
        else
            retriever.release()

        return art
    }

}