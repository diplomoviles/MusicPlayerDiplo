package com.amaurypm.musicplayerdiplo.view.activities

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.musicplayerdiplo.databinding.ActivityMainBinding
import com.amaurypm.musicplayerdiplo.model.MusicFile
import com.amaurypm.musicplayerdiplo.view.adapters.SongsAdapter


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //Para los permisos
    private var readPermissionGranted = false
    private var writePermissionGranted = false

    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    companion object {
        const val PERMISO_ALMACENAMIENTO_EXTERNO = 1
        var musicFiles = ArrayList<MusicFile>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateOrRequestPermissions()
    }

    private fun updateOrRequestPermissions() {

        //Revisando los permisos
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        //Solicitando los permisos

        val permissionsToRequest = mutableListOf<String>()

        if (!readPermissionGranted)
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!writePermissionGranted)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)


        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISO_ALMACENAMIENTO_EXTERNO
            )
        } else {
            //Tengo ambos permisos

            musicFiles = getAllAudio(this)

            if(musicFiles.size >= 1) {
                val songsAdapter = SongsAdapter(this, musicFiles)

                binding.rvSongs.layoutManager =
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                binding.rvSongs.adapter = songsAdapter
            }else{
                //Informar al usuario que no hay archivos de audio reproducibles
            }

        }

    }

    override fun onRestart() {
        super.onRestart()
        updateOrRequestPermissions()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISO_ALMACENAMIENTO_EXTERNO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Se obtuvo el permiso
                    updateOrRequestPermissions()
                } else {
                    if (shouldShowRequestPermissionRationale(permissions[0])) {
                        AlertDialog.Builder(this)
                            .setTitle("Permiso requerido")
                            .setMessage("Se necesita acceder al almacenamiento para obtener el listado de canciones")
                            .setPositiveButton(
                                "Entendido",
                                DialogInterface.OnClickListener { dialog, which ->
                                    updateOrRequestPermissions()
                                })
                            .setNegativeButton(
                                "Salir",
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()
                                    finish()
                                })
                            .create()
                            .show()
                    } else {
                        //Si el usuario no quiere que nunca se le vuelva a preguntar por el permiso
                        Toast.makeText(
                            this,
                            "El permiso de acceso al almacenamiento se ha negado permanentemente",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    fun getAllAudio(context: Context): ArrayList<MusicFile> {
        val tempAudioList = ArrayList<MusicFile>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )

        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val album = cursor.getString(0)
                val title = cursor.getString(1)
                val duration = cursor.getString(2)
                val path = cursor.getString(3)
                val artist = cursor.getString(4)

                val musicFile = MusicFile(path, title, artist, album, duration ?: "0")
                Log.d("MUSICA", "Path: $path - Album: $album")
                tempAudioList.add(musicFile)
            }
            cursor.close()
        }

        return tempAudioList
    }

    fun selectedSong(position: Int){
        //Toast.makeText(this, "Se hizo click en el elemento: $position", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, PlayerActivity::class.java)

        intent.putExtra("position", position)

        startActivity(intent)
    }

}