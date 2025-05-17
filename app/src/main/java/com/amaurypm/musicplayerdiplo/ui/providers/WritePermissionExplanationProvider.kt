package com.amaurypm.musicplayerdiplo.ui.providers

import android.content.Context
import com.amaurypm.musicplayerdiplo.R

//WRITE_EXTERNAL_STORAGE
class WritePermissionExplanationProvider(
    //private val context: Context
): PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de escritura en el almacenamiento externo"
    //override fun getPermissionText(): String = context.getString(R.string.app_name)

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined)
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, habilite el permiso en la configuración de la app"
        else
            "Se requiere el permiso de escritura solamente para acceder a los archivos de audio del dispositivo. No se modificará ningún archivo."
    }
}