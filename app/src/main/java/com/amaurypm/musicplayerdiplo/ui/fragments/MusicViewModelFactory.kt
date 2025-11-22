package com.amaurypm.musicplayerdiplo.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amaurypm.musicplayerdiplo.data.AudioRepository

@Suppress("UNCHECKED_CAST")
class MusicViewModelFactory(
    private val audioRepository: AudioRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MusicViewModel::class.java)){
            return MusicViewModel(audioRepository) as T
        }
        throw IllegalArgumentException("Clase viewmodel desconocida")
    }
}