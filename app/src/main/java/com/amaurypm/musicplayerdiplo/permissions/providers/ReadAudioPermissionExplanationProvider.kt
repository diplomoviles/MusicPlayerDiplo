package com.amaurypm.musicplayerdiplo.permissions.providers


class ReadAudioPermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso para leer los archivos de audio"

    override fun getExplanation(isNotPermanentlyDeclined: Boolean): String {
        return if(isNotPermanentlyDeclined){
            "Se requiere el permiso para acceder a los archivos de audio del dispositivo"
        }else{
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, por favor habilita el permiso desde la configuración de la aplicación."
        }
    }
}

//Con localización se haría más o menos así
/*class ReadAudioPermissionExplanationProvider(private val context: Context): PermissionExplanationProvider {
    override fun getPermissionText(): String = context.getString(R.string.explanation)

    override fun getExplanation(isNotPermanentlyDeclined: Boolean): String {
        return if(isNotPermanentlyDeclined){
            "Se requiere el permiso para acceder a los archivos de audio del dispositivo"
        }else{
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, por favor habilita el permiso desde la configuración de la aplicación."
        }
    }
}*/