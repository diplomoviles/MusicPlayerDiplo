package com.amaurypm.musicplayerdiplo.ui.providers

import android.content.Context
import com.amaurypm.musicplayerdiplo.R

//Para quitar el hard coding en los textos, hay que pasar el contexto a la clase
class ReadAudioPermissionExplanationProvider(private val context: Context): PermissionExplanationProvider {

//class ReadAudioPermissionExplanationProvider(): PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso para leer los archivos de audio"

    //override fun getPermissionText(): String = context.getString(R.string.app_name)

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined) "El permiso se ha negado permanentemente. Para usar la aplicación, favor de habilitar el permiso desde la configuración de la app."
        else "Se requiere el permiso para poder leer los archivos de audio que tiene almacenados"
    }
}