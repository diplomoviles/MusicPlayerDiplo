package com.amaurypm.musicplayerdiplo.ui.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amaurypm.musicplayerdiplo.data.AudioRepository
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicListViewModel(
    private val audioRepository: AudioRepository
): ViewModel() {

    //Cola para los strings de los permisos a solicitar
    private val permissionsToRequestQueue = mutableListOf<String>()
    
    //Ponemos los livedatas o elementos observables
    
    //Livedata para la lista de permisos a observar
    private val _permissionsToRequest = MutableLiveData<MutableList<String>>()
    val permissionsToRequest: LiveData<MutableList<String>> = _permissionsToRequest
    
    //Livedata para la lista de archivos de música
    private val _musicFiles = MutableLiveData<MutableList<MusicFile>>()
    val musicFiles: LiveData<MutableList<MusicFile>> = _musicFiles


    //Función para quitar los permisos de la cola
    fun dismissDialogRemovePermission(){
        if(permissionsToRequestQueue.isNotEmpty())
            permissionsToRequestQueue.removeAt(0) //Quita el primer elemento de cola
    }

    //Para manejar el resultado del permiso
    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ){
        if(!isGranted && !permissionsToRequestQueue.contains(permission)) {
            permissionsToRequestQueue.add(permission)
            _permissionsToRequest.postValue(permissionsToRequestQueue)
        }
    }

    fun getAllAudio(){
        viewModelScope.launch(Dispatchers.IO) {
            _musicFiles.postValue(audioRepository.getAllAudio())
        }
    }

}