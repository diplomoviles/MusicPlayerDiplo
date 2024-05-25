package com.amaurypm.musicplayerdiplo.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amaurypm.musicplayerdiplo.data.local.model.AudioRepository
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.Permission

class MusicListViewModel(private val audioRepository: AudioRepository): ViewModel() {

    //Cola para los strings de los permisos a solicitar
    private val permissionsToRequestQueue = mutableListOf<String>()

    //Livedata para los permisos y se puedan observar
    private val _permissionsToRequest = MutableLiveData<List<String>>()
    val permissionsToRequest: LiveData<List<String>> = _permissionsToRequest

    //Livedata para mi listado de canciones y se pueda observar
    private val _musicFiles = MutableLiveData<List<MusicFile>>()
    val musicFiles: LiveData<List<MusicFile>> = _musicFiles

    fun getAllAudio(){
        viewModelScope.launch(Dispatchers.IO) {
            _musicFiles.postValue(audioRepository.getAllAudio())
        }
    }


    //Función para quitar un permiso de la cola
    fun dismissDialog(){
        if(permissionsToRequestQueue.isNotEmpty())
            permissionsToRequestQueue.removeFirst()
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


}