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
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.databinding.FragmentMusicListBinding
import com.amaurypm.musicplayerdiplo.ui.providers.PermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.ui.providers.ReadAudioPermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.ui.providers.ReadPermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.ui.providers.WritePermissionExplanationProvider

class MusicList : Fragment() {

    private var _binding: FragmentMusicListBinding? = null
    private val binding get() = _binding!!

    private val musicListViewModel: MusicListViewModel by viewModels()

    private var readMediaAudioGranted = false
    private var readPermissionGranted = false
    private var writePermissionGranted = false

    private var permissionsToRequest = mutableListOf<String>()

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->

        //Si todos los permisos están en true, la variable allGranted será true
        val allGranted = permissions.all { map ->
            map.value
        }

        if (allGranted) {
            //Se concedieron todos los permisos exitosamente
            actionPermissionsGranted()
        } else {
            permissionsToRequest.forEach { permission ->
                musicListViewModel.onPermissionResult(
                    permission,
                    permissions[permission] == true
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

        musicListViewModel.permissionsToRequest.observe(viewLifecycleOwner){ queue ->

            queue.reversed().forEach(){ permission ->
                //Pongo el diálogo para dar mis razones al usuario sobre los permisos.

                showPermissionExplanationDialog(
                    when(permission){
                        Manifest.permission.READ_EXTERNAL_STORAGE -> ReadPermissionExplanationProvider()
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> WritePermissionExplanationProvider()
                        Manifest.permission.READ_MEDIA_AUDIO -> ReadAudioPermissionExplanationProvider(requireContext())
                        else -> return@forEach
                    }
                    ,
                    !shouldShowRequestPermissionRationale(permission),
                    { //Lambda onDismiss
                        musicListViewModel.dismissDialog()
                    },
                    { //Lambda onClick
                        musicListViewModel.dismissDialog()
                        updateOrRequestPermissions()
                    },
                    { //Lambda onGoToAppSettings
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        updateOrRequestPermissions()
    }

    private fun updateOrRequestPermissions() {

        //Revisamos los permisos

        /*
        Para API Level del 23 al 28 (Android 6 al 9)
        READ_EXTERNAL_STORAGE y WRITE_EXTERNAL_STORAGE

       	Para API Level del 29 al 32 (Android 10 al 12)
        READ_EXTERNAL_STORAGE

        Para API Level del 33 en adelante (Android 13 en adelante)
        READ_MEDIA_AUDIO
        */


        val hasReadPermission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasWritePermission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasMediaAudioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29
        readMediaAudioGranted = hasMediaAudioPermission

        if (!readPermissionGranted)
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!writePermissionGranted)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (!readMediaAudioGranted && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)

        if (permissionsToRequest.isNotEmpty()) {
            //Pedimos los permisos
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            //Todos los permisos están concedidos
            actionPermissionsGranted()
        }


    }

    private fun actionPermissionsGranted() {
        Toast.makeText(
            requireContext(),
            "Todos los permisos han sido concedidos",
            Toast.LENGTH_SHORT
        ).show()
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
            .setPositiveButton(if(isPermanentlyDeclined) "Configuración" else "Aceptar"){ _, _ ->
                if(isPermanentlyDeclined){
                    onGoToAppSettings()
                }
                else {
                    onOkClick()
                }
            }
            .setOnDismissListener { _ ->
                onDismiss()
            }
            .show()
    }

}