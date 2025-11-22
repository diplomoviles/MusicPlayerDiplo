package com.amaurypm.musicplayerdiplo.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amaurypm.musicplayerdiplo.data.AudioRepository
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicViewModel(
    private val audioRepository: AudioRepository
): ViewModel() {

    //Cola para los strings de los permisos a solicitar
    private val permissionToRequestQueue = mutableListOf<String>()

    //Contenedores LiveData para que se puedan observar
    
    //LiveData para la lista de los permisos, pero esta se puede observar desde la UI
    private val _permissionToRequest = MutableLiveData<MutableList<String>>()
    val permissionToRequest: LiveData<MutableList<String>> = _permissionToRequest
    
    //LiveData para la lista de los archivos de música y que se puedan observar desde la UI
    private val _musicFiles = MutableLiveData<MutableList<MusicFile>>() 
    val musicFiles: LiveData<MutableList<MusicFile>> = _musicFiles

    //Función para quitar los permisos de la cola
    fun dismissDialogRemovePermission(){
        if(permissionToRequestQueue.isNotEmpty())
            permissionToRequestQueue.removeAt(0)
    }

    //Función para actualizar el resultado de los permisos
    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ){
        if(!isGranted && !permissionToRequestQueue.contains(permission)){
            permissionToRequestQueue.add(permission)
            _permissionToRequest.postValue(permissionToRequestQueue)
        }
    }

    fun getAllAudio(){
        viewModelScope.launch(Dispatchers.IO) {
            _musicFiles.postValue(audioRepository.getAllAudio())
        }
    }

}