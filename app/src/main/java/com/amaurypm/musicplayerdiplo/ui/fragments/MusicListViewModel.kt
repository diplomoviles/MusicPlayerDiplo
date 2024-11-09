package com.amaurypm.musicplayerdiplo.ui.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile

class MusicListViewModel(

): ViewModel() {

    //Cola para los strings de los permisos a solicitar
    private val permissionsToRequestQueue = mutableListOf<String>()
    
    //Ponemos los livedatas o elementos observables
    
    //Livedata para la lista de permisos a observar
    private val _permissionsToRequest = MutableLiveData<MutableList<String>>()
    val permissionsToRequest: LiveData<MutableList<String>> = _permissionsToRequest
    
    //Livedata para la lista de archivos de música
    private val _musicFiles = MutableLiveData<MusicFile>()
    val musicFiles: LiveData<MusicFile> = _musicFiles


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





}