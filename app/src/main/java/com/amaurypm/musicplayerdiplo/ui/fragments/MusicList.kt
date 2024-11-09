package com.amaurypm.musicplayerdiplo.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    //Instanciamos el viewmodel con el audiorepository mediante el factory
    private val musicListViewModel: MusicListViewModel by viewModels{
        MusicListViewModelFactory(AudioRepository(requireContext()))
    }

    private var readMediaAudioGranted = false //READ_MEDIA_AUDIO
    private var readPermissionGranted = false //READ_EXTERNAL_STORAGE
    private var writePermissionGranted = false //WRITE_EXTERNAL_STORAGE

    private var permissionsToRequest = mutableListOf<String>()


    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permissionsMap: Map<String, Boolean> ->
        //Usando una función de extensión para saber si todos los valores del mapa están en true
        val allGranted = permissionsMap.all{ map ->
            map.value
        }

        if(allGranted){
            //Tenemos todos los permisos necesarios!!!!!
            actionPermissionGranted()
        }else{
            //Nos falta por lo menos uno de los permisos necesarios
            permissionsToRequest.forEach { permission ->
                musicListViewModel.onPermissionResult(
                    permission = permission,
                    isGranted = permissionsMap[permission] == true
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMusicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Con esta instrucción verificamos si tenemos el permiso o no
        /*ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED*/

        musicListViewModel.permissionsToRequest.observe(viewLifecycleOwner){ queue ->

            queue.reversed().forEach { permission ->
                showPermissionExplanationDialog(
                    when(permission){
                        Manifest.permission.READ_MEDIA_AUDIO -> ReadAudioPermissionExplanationProvider()
                        Manifest.permission.READ_EXTERNAL_STORAGE -> ReadPermissionExplanationProvider()
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> WritePermissionExplanationProvider()
                        else -> return@forEach
                    },
                    !shouldShowRequestPermissionRationale(permission),
                    {
                        musicListViewModel.dismissDialogRemovePermission() //Quito el permiso de la lista. Si hace falta se agrega después
                    },
                    {
                        musicListViewModel.dismissDialogRemovePermission()
                        updateOrRequestPermissions()
                    },
                    {
                        startActivity(
                            //Código para ir a los settings de la app en específico
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        updateOrRequestPermissions()
    }

    private fun updateOrRequestPermissions(){

        //Primeramente reviso los permisos que tengo

        //Revisando el READ_EXTERNAL_STORAGE
        readPermissionGranted = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
                    PackageManager.PERMISSION_GRANTED
        }else{
            true
        }

        //Revisando el WRITE_EXTERNAL_STORAGE
        writePermissionGranted = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) ==
                    PackageManager.PERMISSION_GRANTED
        }else{
            true
        }

        //Revisando el READ_MEDIA_AUDIO
        readMediaAudioGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) ==
                    PackageManager.PERMISSION_GRANTED
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
            //Tengo permisos por pedir
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }else{
            //Tenemos todos los permisos necesarios!!!!!
            actionPermissionGranted()
        }
    }

    private fun actionPermissionGranted(){
        /*Toast.makeText(
            requireContext(),
            "Todos los permisos se han concedido!!",
            Toast.LENGTH_SHORT
        ).show()*/

        musicListViewModel.getAllAudio()

        musicListViewModel.musicFiles.observe(viewLifecycleOwner){ songs ->
            if(songs.isNotEmpty()){
                //Hay por lo menos un archivo reproducible
                val songsAdapter = SongsAdapter(songs){ position ->
                    //Manejamos el click de la canción

                    //Mandamos al Media Player
                    findNavController().navigate(MusicListDirections.actionMusicListToMusicPlayer(
                        position
                    ))

                }

                binding.rvSongs.apply {
                    layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                    adapter = songsAdapter
                }

            }
        }
    }

    private fun showPermissionExplanationDialog(
        permissionExplanationProvider: PermissionExplanationProvider,
        isPermanentlyDeclined: Boolean,
        onDismiss: () -> Unit,
        onOkClick: () -> Unit,
        onGoToAppSettingsClick: () -> Unit
    ){
        AlertDialog.Builder(requireContext())
            .setTitle(permissionExplanationProvider.getPermissionText())
            .setMessage(permissionExplanationProvider.getExplanation(isPermanentlyDeclined))
            .setPositiveButton(if(isPermanentlyDeclined) "Configuración" else "Entendido"){ dialog, _ ->
                if(isPermanentlyDeclined) onGoToAppSettingsClick()
                else onOkClick()
            }
            .setOnDismissListener{ _ ->
                onDismiss()
            }
            .show()
    }

}