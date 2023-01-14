package com.amaurypm.musicplayerdiplo.view.adapters

import android.content.Context
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.databinding.MusicItemBinding
import com.amaurypm.musicplayerdiplo.model.MusicFile
import com.amaurypm.musicplayerdiplo.view.activities.MainActivity
import com.bumptech.glide.Glide

/**
 * Creado por Amaury Perea Matsumura el 13/01/23
 */
class SongsAdapter(private val context: Context, val songs: ArrayList<MusicFile>): RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    class ViewHolder(view: MusicItemBinding): RecyclerView.ViewHolder(view.root) {
        val ivSongImage = view.ivSongImage
        val tvSongName = view.tvSongName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MusicItemBinding.inflate(layoutInflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvSongName.text = songs[position].title

        var image: ByteArray? = null

        try{
            image = songs[position].path?.let { getAlbumArt(it) }
        }catch(e: Exception){
            e.printStackTrace()
        }

        if(image!=null){
            Glide.with(context).asBitmap()
                .load(image)
                .into(holder.ivSongImage)
        }else{   //No tiene una imagen incrustada
            Glide.with(context)
                .load(R.drawable.ic_song)
                .into(holder.ivSongImage)
        }

        //Para manejar los clicks
        holder.itemView.setOnClickListener {
            if(context is MainActivity) context.selectedSong(position)
        }
    }

    override fun getItemCount(): Int = songs.size

    fun getAlbumArt(uri: String): ByteArray?{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }

}


