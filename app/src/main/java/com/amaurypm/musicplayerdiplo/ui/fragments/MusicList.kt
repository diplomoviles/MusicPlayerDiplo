package com.amaurypm.musicplayerdiplo.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.data.AudioRepository
import com.amaurypm.musicplayerdiplo.databinding.FragmentMusicListBinding
import com.amaurypm.musicplayerdiplo.ui.adapters.SongsAdapter
import com.amaurypm.musicplayerdiplo.ui.providers.PermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.ui.providers.ReadAudioPermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.ui.providers.ReadPermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.ui.providers.WritePermissionExplanationProvider

class MusicList : Fragment() {

    private var _binding: FragmentMusicListBinding? = null
    private val binding get() = _binding!!

    //Instanciando el viewmodel
    private val musicViewModel: MusicViewModel by viewModels{
        MusicViewModelFactory(AudioRepository(requireContext()))
    }

    private var readMediaAudioGranted = false   //Read media audio
    private var readPermissionGranted = false   //Read external storage
    private var writePermissionGranted = false  //Write permission storage

    private var permissionsToRequest = mutableListOf<String>()

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap: Map<String, Boolean> ->

        val allGranted = permissionsMap.all { map ->
            map.value
        }

        if(allGranted){
            //Se concedieron todos los permisos
            actionPermissionGranted()
        }else{
            //Hay algún permiso que se denegó
            permissionsToRequest.forEach { permission ->
                musicViewModel.onPermissionResult(
                    permission = permission,
                    isGranted = permissionsMap[permission] == true
                )
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMusicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Nos suscribimos al livedata de los permisos
        musicViewModel.permissionsToRequest.observe(viewLifecycleOwner) { queue ->
            queue.reversed().forEach { permission ->
                showPermissionExplanationDialog(
                    when(permission){
                        Manifest.permission.READ_EXTERNAL_STORAGE -> {
                            ReadPermissionExplanationProvider()
                        }
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            WritePermissionExplanationProvider()
                        }
                        Manifest.permission.READ_MEDIA_AUDIO -> {
                            ReadAudioPermissionExplanationProvider()
                        }
                        else -> return@forEach
                    },
                    !shouldShowRequestPermissionRationale(permission),
                    { //onDismiss
                        musicViewModel.dismissDialogRemovePermission()
                    },{ //onOkClick
                        musicViewModel.dismissDialogRemovePermission()
                        updateOrRequestPermissions()
                    },{ //onGoToAppSettings
                        //Ponemos el atajo para los settings de la app
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", requireContext().packageName, null)
                            )
                        )
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateOrRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun updateOrRequestPermissions(){

        /*ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED*/

        //Revisando si tengo los permisos

        //Permiso de READ_EXTERNAL_STORAGE
        readPermissionGranted = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE  //importar de android.Manifest
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }

        //Permiso de WRITE_EXTERNAL_STORAGE
        writePermissionGranted = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE  //importar de android.Manifest
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }

        //Permiso de READ_MEDIA_AUDIO
        readMediaAudioGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO  //importar de android.Manifest
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }

        if(!readPermissionGranted)
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if(!writePermissionGranted)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(!readMediaAudioGranted && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)

        if(permissionsToRequest.isNotEmpty()){
            //Hay permisos por pedir todavía
            //ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest.toTypedArray(), 1)
            permissionsLauncher.launch(
                permissionsToRequest.toTypedArray()
            )
        }else{
            //Todos los permisos que necesitaba se han concedido!!!
            actionPermissionGranted()
        }

    }

    private fun actionPermissionGranted(){
        /*Toast.makeText(
            requireContext(),
            "Todos los permisos necesarios se han concedido",
            Toast.LENGTH_SHORT
        ).show()*/
        musicViewModel.getAllAudio()

        musicViewModel.musicFiles.observe(viewLifecycleOwner) { songs ->
            //Instanciamos nuestro adapter y cargamos el recycler view
            if(songs.isNotEmpty()){
                val songsAdapter = SongsAdapter(songs){ position ->
                    //Manejamos el click a los elementos
                    findNavController().navigate(MusicListDirections.actionMusicListToMusicPlayer(
                        position
                    ))
                }

                binding.rvSongs.layoutManager = LinearLayoutManager(requireContext())
                binding.rvSongs.adapter = songsAdapter

                /*binding.apply {
                    rvSongs.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = songsAdapter
                    }
                }*/
            }
        }
    }

    private fun showPermissionExplanationDialog(
        permissionExplanationProvider: PermissionExplanationProvider,
        isPermanentlyDeclined: Boolean,
        onDismiss: () -> Unit,
        onOkClick: () -> Unit,
        onGoToAppSettings: () -> Unit
    ){
        AlertDialog.Builder(requireContext())
            .setTitle(permissionExplanationProvider.getPermissionText())
            .setMessage(permissionExplanationProvider.getExplanation(isPermanentlyDeclined))
            .setPositiveButton(if(isPermanentlyDeclined) "Configuración" else "Entendido") { _, _ ->
                if(isPermanentlyDeclined) onGoToAppSettings()
                else onOkClick()
            }
            .setOnDismissListener { _ ->
                onDismiss()
            }
            .show()
    }

}