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
import javax.microedition.khronos.opengles.GL

class SongAdapter(
    private val songs: List<MusicFile>,
    private val onSongClick: (position: Int) -> Unit
): RecyclerView.Adapter<SongsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsViewHolder(binding)
    }

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {

        holder.bind(songs[position])

        holder.itemView.setOnClickListener{
            onSongClick(position)
        }

        var image: ByteArray? = null

        try{
            image = getAlbumArt(songs[position].path)
        }catch(e: Exception){
            //Manejo del error
            e.printStackTrace()
        }

        if(image != null){
            Glide.with(holder.itemView.context).asBitmap()
                .load(image)
                .into(holder.ivSongImage)
        }else{
            holder.ivSongImage.setImageResource(R.drawable.ic_song)
        }


    }

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