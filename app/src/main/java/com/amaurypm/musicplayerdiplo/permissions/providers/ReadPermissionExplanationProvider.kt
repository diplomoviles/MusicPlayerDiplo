package com.amaurypm.musicplayerdiplo.permissions.providers

class ReadPermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de lectura del almacenamiento externo"

    override fun getExplanation(isNotPermanentlyDeclined: Boolean): String {
        return if(isNotPermanentlyDeclined){
            "Se requiere el permiso de lectura para acceder a los archivos de audio del dispositivo"
        }else{
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, por favor habilita el permiso desde la configuración de la aplicación."
        }
    }
}