package com.amaurypm.musicplayerdiplo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import com.amaurypm.musicplayerdiplo.databinding.MusicItemBinding

class SongsAdapter(
    private val songs: MutableList<MusicFile>,
    private val onSongClick: (Int) -> Unit
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

    }

}