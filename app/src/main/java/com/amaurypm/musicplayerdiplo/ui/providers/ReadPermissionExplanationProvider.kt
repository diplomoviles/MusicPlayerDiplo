package com.amaurypm.musicplayerdiplo.ui.providers

//READ_EXTERNAL_STORAGE
class ReadPermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de lectura de almacenamiento externo"

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined)
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, habilite el permiso en la configuración de la app"
        else
            "Se requiere el permiso de lectura solamente para acceder a los archivos de audio del dispositivo"
    }
}