package com.amaurypm.musicplayerdiplo.ui.providers

class WritePermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de escritura del almacenamiento externo"

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined) "El permiso se ha negado permanentemente. Para usar la aplicación, favor de habilitar el permiso desde la configuración de la app."
        else "No se va a modificar ningún archivo, sino que se requiere el permiso únicamente para poder tener acceso a los archivos de audio"
    }
}