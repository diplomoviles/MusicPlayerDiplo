package com.amaurypm.musicplayerdiplo.ui.providers

class ReadPermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de lectura de almacenamiento externo"

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined) "El permiso se ha negado permanentemente. Para usar la aplicación, favor de habilitar el permiso desde la configuración de la app."
        else "Se requiere el permiso de lectura para acceder a los archivos de audio del dispositivo"
    }
}