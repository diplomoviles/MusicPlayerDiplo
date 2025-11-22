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
import com.amaurypm.musicplayerdiplo.data.AudioRepository
import com.amaurypm.musicplayerdiplo.databinding.FragmentMusicListBinding
import com.amaurypm.musicplayerdiplo.permissions.providers.PermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.permissions.providers.ReadAudioPermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.permissions.providers.ReadPermissionExplanationProvider
import com.amaurypm.musicplayerdiplo.permissions.providers.WritePermissionExplanationProvider


class MusicList : Fragment() {

    private var _binding: FragmentMusicListBinding? = null
    private val binding get() = _binding!!

    //Instanciamos el viewmodel
    private val musicViewModel: MusicViewModel by viewModels()

    //Variables booleanas para el estado de los permisos
    private var readPermissionGranted = false //Read external storage
    private var writePermissionGranted = false //Write external storage
    private var readMediaAudioGranted = false //Read media audio

    private var permissionsToRequest = mutableListOf<String>()

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult: Map<String, Boolean> ->
        //allGranted será true si todos los elementos de la colección están en true
        val allGranted = permissionsResult.all { map ->
            map.value
        }

        if (allGranted) {
            //Se concedieron todos los permisos después de pedirlos!!!!
            actionPermissionsGranted()
        } else {
            //Hay por lo menos un permiso no concedido
            permissionsToRequest.forEach { permission ->
                musicViewModel.onPermissionResult(
                    permission = permission,
                    isGranted = permissionsResult[permission] == true
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMusicListBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    //Este se ejecuta ya cuando el fragment está visible en pantalla
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Observamos la cola de permisos del viewModel
        musicViewModel.permissionToRequest.observe(viewLifecycleOwner) { queue ->
            queue.reversed().forEach { permission ->
                //Como el usuario negó uno o más permisos, generamos tantos Dialogs
                //con explicaciones como se requiera
                showPermissionExplanationDialog(
                    when (permission) {
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
                    shouldShowRequestPermissionRationale(permission),
                    {
                        musicViewModel.dismissDialogRemovePermission()
                    }, {
                        musicViewModel.dismissDialogRemovePermission()
                        updateOrRequestPermissions()
                    }, {
                        //Mandamos un intent a la configuración de la app
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts(
                                    "package",
                                    requireContext().packageName,
                                    null
                                )
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateOrRequestPermissions() {
        //Primero reviso qué permisos tengo

        //Para el Read external storage
        readPermissionGranted = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        //Para el write external storage
        writePermissionGranted = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        //Para el read media audio
        readMediaAudioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        //Si no tenemos alguno de los permisos, lo agregamos al listado
        if (!readPermissionGranted)
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!writePermissionGranted)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (!readMediaAudioGranted)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)

        //Revisamos el listado de los permisos
        if (permissionsToRequest.isNotEmpty()) {
            //Pedimos los permisos que hacen falta
            permissionsLauncher.launch(
                permissionsToRequest.toTypedArray()
            )
        } else {
            //Tenemos todos los permisos!!!!!
            actionPermissionsGranted()
        }
    }

    private fun actionPermissionsGranted() {
        /*Toast.makeText(
            requireContext(),
            "Todos los permisos se han concedido. Ehhhhh!!",
            Toast.LENGTH_SHORT
        ).show()*/
        AudioRepository(requireContext()).getAllAudio()
    }

    private fun showPermissionExplanationDialog(
        permissionExplanationProvider: PermissionExplanationProvider,
        isNotPermanentlyDeclined: Boolean,
        onDismiss: () -> Unit,
        onOkClick: () -> Unit,
        onGoToAppSettingsClick: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(permissionExplanationProvider.getPermissionText())
            .setMessage(permissionExplanationProvider.getExplanation(isNotPermanentlyDeclined))
            .setPositiveButton(if (isNotPermanentlyDeclined) "Entendido" else "Configuración") { dialog, _ ->
                dialog.dismiss()
                if (isNotPermanentlyDeclined) onOkClick()
                else onGoToAppSettingsClick()
            }
            .setOnDismissListener { dialog ->
                dialog.dismiss()
                onDismiss()
            }
            .show()
    }

}