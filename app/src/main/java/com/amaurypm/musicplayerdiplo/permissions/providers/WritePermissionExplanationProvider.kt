package com.amaurypm.musicplayerdiplo.permissions.providers

class WritePermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de escritura del almacenamiento externo"

    override fun getExplanation(isNotPermanentlyDeclined: Boolean): String {
        return if(isNotPermanentlyDeclined){
            "Solamente se requiere el permiso de escritura para acceder a los archivos de audio del dispositivo. Ningún archivo será modificado."
        }else{
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, por favor habilita el permiso desde la configuración de la aplicación."
        }
    }
}