package com.amaurypm.musicplayerdiplo.ui.providers

class ReadPermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de lectura del almacenamiento externo"

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined)
            "El permiso se ha negado permanentemente. Para usar esta aplicación, habilite el permiso en la configuración"
        else
            "Se requiere el permiso de lectura solamente para acceder a los archivos de audio del dispositivo"
    }
}