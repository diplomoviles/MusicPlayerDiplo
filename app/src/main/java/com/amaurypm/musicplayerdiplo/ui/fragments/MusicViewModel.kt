package com.amaurypm.musicplayerdiplo.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import java.security.Permission

class MusicViewModel: ViewModel() {

    //Cola para los strings de los permisos a solicitar
    private val permissionsToRequestQueue = mutableListOf<String>()
    
    //Livedata para la lista de los permisos y que se pueda observar
    private val _permissionsToRequest = MutableLiveData<MutableList<String>>()
    val permissionsToRequest: LiveData<MutableList<String>> = _permissionsToRequest
    
    //Livedata para la lista de archivos de música que podamos observar
    private val _musicFiles = MutableLiveData<MutableList<MusicFile>>() 
    val musicFiles: LiveData<MutableList<MusicFile>> = _musicFiles

    //Agregando las funciones a acceder desde la UI

    //Función para quitar permisos de la cola de permisos
    fun dismissDialogRemovePermission(){
        if(permissionsToRequestQueue.isNotEmpty()){
            permissionsToRequestQueue.removeAt(0)
        }
    }

    //Función para manejar el resultado de los permisos
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