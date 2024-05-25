package com.amaurypm.musicplayerdiplo.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import com.amaurypm.musicplayerdiplo.databinding.MusicItemBinding

class SongsViewHolder(
    private val binding: MusicItemBinding
): RecyclerView.ViewHolder(binding.root) {

    val ivSongImage = binding.ivSongImage

    fun bind(song: MusicFile){
        binding.tvSongName.text = song.title
    }

}