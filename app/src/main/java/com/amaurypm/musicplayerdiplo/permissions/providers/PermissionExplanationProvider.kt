package com.amaurypm.musicplayerdiplo.permissions.providers

interface PermissionExplanationProvider {
    fun getPermissionText(): String
    fun getExplanation(isNotPermanentlyDeclined: Boolean): String
}