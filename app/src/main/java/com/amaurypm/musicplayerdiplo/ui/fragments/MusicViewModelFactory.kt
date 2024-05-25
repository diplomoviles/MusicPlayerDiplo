package com.amaurypm.musicplayerdiplo.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amaurypm.musicplayerdiplo.data.local.model.AudioRepository

@Suppress("UNCHECKED_CAST")
class MusicViewModelFactory(
    private val audioRepository: AudioRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MusicListViewModel::class.java)){
            return MusicListViewModel(audioRepository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }

}