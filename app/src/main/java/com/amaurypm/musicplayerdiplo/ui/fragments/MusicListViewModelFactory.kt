package com.amaurypm.musicplayerdiplo.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amaurypm.musicplayerdiplo.data.AudioRepository

class MusicListViewModelFactory(
    private val audioRepository: AudioRepository,
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MusicListViewModel::class.java)){
            return MusicListViewModel(audioRepository) as T
        }
        throw IllegalArgumentException("Clase viewmodel desconocida")
    }

}