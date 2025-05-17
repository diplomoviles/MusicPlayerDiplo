package com.amaurypm.musicplayerdiplo.ui.providers

//READ_MEDIA_AUDIO
class ReadAudioPermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso para leer los archivos de audio"

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined)
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, habilite el permiso en la configuración de la app"
        else
            "Se requiere el permiso solamente para acceder a los archivos de audio del dispositivo"
    }
}