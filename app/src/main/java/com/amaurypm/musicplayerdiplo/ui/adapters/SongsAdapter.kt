package com.amaurypm.musicplayerdiplo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import com.amaurypm.musicplayerdiplo.databinding.MusicItemBinding

class SongsAdapter(
    private val songs: MutableList<MusicFile>,
    private val onSongClick: (Int) -> Unit
): RecyclerView.Adapter<SongViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int
    ) {

        val song = songs[position]

        holder.bind(song)

        holder.itemView.setOnClickListener {
            onSongClick(position)
        }

    }

    override fun getItemCount(): Int = songs.size
}