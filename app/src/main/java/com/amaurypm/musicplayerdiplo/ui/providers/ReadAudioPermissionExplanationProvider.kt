package com.amaurypm.musicplayerdiplo.ui.providers

import android.content.Context
import com.amaurypm.musicplayerdiplo.R

class ReadAudioPermissionExplanationProvider(
    //private val context: Context
): PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso para leer los archivos de audio"
    //override fun getPermissionText(): String = context.getString(R.string.music_player_diplo)

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined)
            "El permiso se ha negado permanentemente. Para usar esta aplicación, habilite el permiso en la configuración"
        else
            "Se requiere el permiso solamente para acceder a los archivos de audio del dispositivo"
    }
}