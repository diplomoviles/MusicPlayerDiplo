package com.amaurypm.musicplayerdiplo.ui.fragments

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.data.AudioRepository
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import com.amaurypm.musicplayerdiplo.databinding.FragmentMusicPlayerBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class MusicPlayer : Fragment(), OnCompletionListener {

    private var _binding: FragmentMusicPlayerBinding? = null
    private val binding get() = _binding!!

    //Instanciamos el viewmodel con el factory y el audiorepository
    private val musicListViewModel: MusicListViewModel by viewModels{
        MusicListViewModelFactory(AudioRepository(requireContext()))
    }

    //Para el manejo de nuestro player y que tengan alcance global
    private lateinit var mediaPlayer: MediaPlayer
    private var position = -1
    private var song: MusicFile? = null
    private var playing = false //Si la canción estaba reproduciéndose
    private var isShuffleOn = false //Para saber si el modo aleatorio está activo
    private var isRepeatOn = false //Para saber si el modo repeat está activo
    private var audioList = mutableListOf<MusicFile>() //Para el listado de canciones


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMusicPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: MusicPlayerArgs by navArgs()

        position = args.position

        musicListViewModel.getAllAudio()

        musicListViewModel.musicFiles.observe(viewLifecycleOwner){ songs ->
            //Establezco el listado de canciones y un objecto para la canción a reproducir
            audioList = songs
            song = songs[position]
            //setMetaData(song!!)

            //Ponemos el floating action button con la imagen de pausa
            binding.fabPlayPause.setImageResource(R.drawable.ic_pause)

            prepareSong(song, true)

            //Para que el usuario pueda manipular la seekbar
            binding.sbSong.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser){ //Si el usuario hace un gesto para modificar la seekbar
                        mediaPlayer.seekTo(progress*1000) //Lo regresamos a milisegundos
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}

            })


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mediaPlayer.release()
    }

    override fun onStart() {
        super.onStart()
        if(playing)
            mediaPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        playing = mediaPlayer.isPlaying
        mediaPlayer.pause()
    }


    //62 ->  1:02
    //95 ->  3:15
    //35 ->  0:35

    private fun formattedTime(currentPosition: Int): String{ //Recibe la posición en segundos
        var totalOut = ""
        var totalNew = ""
        val seconds  = "${currentPosition%60}"
        val minutes  = "${currentPosition/60}"
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"

        return if(seconds.length == 1) totalNew
        else totalOut
    }

    private fun setMetaData(song: MusicFile){
        var cover: ByteArray? = null
        try{
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(song.data)

            cover = retriever.embeddedPicture

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                retriever.close()
            else
                retriever.release()

        }catch (e: Exception){
            e.printStackTrace()
        }

        Glide.with(requireContext())
            .load(cover)
            .error(R.drawable.cover)
            .into(binding.ivCover)

        //Establecemos la duración de la canción en la UI
        val duration = song.duration.toInt()/1000  //Transformamos la duración de milisegundos a segundos
        binding.tvDuration.text = formattedTime(duration)

    }

    override fun onCompletion(mp: MediaPlayer?) {
        //Aquí programamos cuando se termine de reproducir un contenido
    }

    //Para tener una canción preparada y lista para reproducir
    private fun prepareSong(
        selectedSong: MusicFile?,
        start: Boolean  //Valor para saber si comenzamos a reproducir la canción o no
    ){
        selectedSong?.let { song ->
            binding.apply {
                tvArtistName.text = song.artist
                tvSongName.text = song.title

                //Instanciamos nuestro mediaplayer con un contenido a reproducir
                mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(song.data))

                mediaPlayer.setOnCompletionListener(this@MusicPlayer)

                sbSong.max = mediaPlayer.duration/1000  //Le pasamos la duración en segundos

                setMetaData(song)

                //Actualizamos la seekbar cada segundo
                lifecycleScope.launch(Dispatchers.Main) {
                    while(isActive){
                        //Pasamos la posición actual a segundos
                        val currentPosition = mediaPlayer.currentPosition/1000
                        sbSong.progress = currentPosition
                        tvTime.text = formattedTime(currentPosition)
                        delay(1000)
                    }
                }

                if(start){
                    mediaPlayer.start()
                    playing = mediaPlayer.isPlaying
                }


            }
        }
    }
}