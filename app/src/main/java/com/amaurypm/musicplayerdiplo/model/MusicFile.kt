package com.amaurypm.musicplayerdiplo.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Creado por Amaury Perea Matsumura el 07/01/23
 */

@Parcelize
data class MusicFile(
    val path: String?,
    val title: String?,
    val artist: String?,
    val album: String?,
    val duration: String?
):Parcelable