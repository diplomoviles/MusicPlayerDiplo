package com.amaurypm.musicplayerdiplo.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile

class SongsAdapter(
    private val songs: MutableList<MusicFile>,
    private val onSongClick: (Int) -> Unit
): RecyclerView.Adapter<SongViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongViewHolder =
        SongViewHolder.create(
            parent,
            onSongClick
        )

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int
    ) {
        holder.bind(songs[position], position)
    }

    override fun getItemCount(): Int = songs.size
}