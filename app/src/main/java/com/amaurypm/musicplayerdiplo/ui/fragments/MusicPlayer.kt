package com.amaurypm.musicplayerdiplo.ui.fragments

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
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

class MusicPlayer : Fragment(), MediaPlayer.OnCompletionListener, View.OnClickListener {

    private var _binding: FragmentMusicPlayerBinding? = null
    private val binding get() = _binding!!

    //Instanciamos el viewModel
    private val musicViewModel: MusicViewModel by viewModels {
        MusicViewModelFactory(AudioRepository(requireContext()))
    }

    private lateinit var mediaPlayer: MediaPlayer
    private var position = -1
    private var song: MusicFile? = null
    private var playing = false
    private var isShuffleOn = false
    private var isRepeatOn = false
    private var audioList = mutableListOf<MusicFile>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMusicPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Recuperamos el argumento recibido
        val args: MusicPlayerArgs by navArgs()

        position = args.position

        musicViewModel.getAllAudio()

        musicViewModel.musicFiles.observe(viewLifecycleOwner) { songs ->
            audioList = songs
            song = songs[position]  //Canción que el usuario seleccionó en la pantalla previa

            //Ponemos el botón de inicio en pausa
            binding.fabPlayPause.setImageResource(R.drawable.ic_pause)

            /*binding.fabPlayPause.setOnClickListener {

            }

            binding.ivNext.setOnClickListener {

            }*/

            binding.fabPlayPause.setOnClickListener(this)
            binding.ivShuffle.setOnClickListener(this)
            binding.ivRepeat.setOnClickListener(this)
            binding.ivPrev.setOnClickListener(this)
            binding.ivNext.setOnClickListener(this)

            prepareSong(song!!, true)

            //Para que la seekbar responda a cambios del usuario
            binding.sbSong.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                   //Si el usuario la movió
                   if(fromUser){
                       mediaPlayer.seekTo(progress*1000) //regresamos el dato a milisegundos para el MediaPlayer
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

    private fun formattedTime(currentMilisPosition: Int): String {
        val currentPosition = currentMilisPosition / 1000  //Lo pasamos a segundos
        var totalOut = ""
        var totalNew = ""
        val seconds = "${currentPosition % 60}"
        val minutes = "${currentPosition / 60}"
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"

        return if (seconds.length == 1) totalNew
        else totalOut

        //198000 milisegundos -> 198 segundos  -> 3:18
        //20000 milisegundos -> 20 segundos -> 0:20
    }

    private fun setMetaData(song: MusicFile) {

        var cover: ByteArray? = null
        try {

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(song.data)
            cover = retriever.embeddedPicture

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                retriever.close()
            else
                retriever.release()

        } catch (e: Exception) {
            //Manejamos la excepción
            e.printStackTrace()
        }

        Glide.with(requireContext())
            .load(cover)
            .error(R.drawable.cover)
            .into(binding.ivCover)

        binding.tvDuration.text = formattedTime(song.duration.toInt())
        binding.tvSongName.text = song.title
        binding.tvArtistName.text = song.artist

    }

    //Es para preparar la canción y si la tenemos que comenzar a reproducir o no
    private fun prepareSong(selectedSong: MusicFile, start: Boolean) {

        selectedSong.let { song ->
            //Instanciamos el mediaplayer
            mediaPlayer = MediaPlayer.create(requireContext(), song.data.toUri())

            mediaPlayer.setOnCompletionListener(this)

            //Partimos el seekbar en secciones por segundo
            binding.sbSong.max = mediaPlayer.duration / 1000

            setMetaData(song)

            //Para actualizar la seekbar que se tiene que cambie cada segundo
            lifecycleScope.launch(Dispatchers.Main) {
                while (isActive) {
                    binding.sbSong.progress = mediaPlayer.currentPosition / 1000
                    binding.tvTime.text = formattedTime(mediaPlayer.currentPosition)
                    delay(1000)
                }
            }

            if (start) {
                mediaPlayer.start()
                playing = mediaPlayer.isPlaying
            }
        }


    }

    //Este se ejecuta cuando se termina de reproducir un contenido
    override fun onCompletion(mediaPlayer: MediaPlayer?) {

    }

    override fun onClick(view: View?) {
        when(view){
            binding.fabPlayPause -> togglePlayPause()
            binding.ivShuffle -> {}
            binding.ivRepeat -> {}
            binding.ivPrev -> playNextOrPrevious(isNext = false)
            binding.ivNext -> playNextOrPrevious(isNext = true)
        }
    }

    private fun togglePlayPause(){
        if(mediaPlayer.isPlaying){
            mediaPlayer.pause()
            binding.fabPlayPause.setImageResource(R.drawable.ic_play)
        }else{
            mediaPlayer.start()
            binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun updatePosition(isNext: Boolean){
        position = when{
            //Shuffle está encendido      10 canciones, va de 0 al 9
            isShuffleOn && !isRepeatOn -> (0 until audioList.size).random()
            //Normal
            !isShuffleOn && !isRepeatOn ->
                if(isNext) (position + 1) % audioList.size //10%10 = 0
                else (position -1 + audioList.size) % audioList.size  //0, 9%10 -> 9
            //Repeat encendido
            else -> position
        }
    }

    private fun playNextOrPrevious(isNext: Boolean){
        updatePosition(isNext)
        song = audioList[position]
        playing = mediaPlayer.isPlaying
        mediaPlayer.release()
        prepareSong(song!!, playing)
        binding.fabPlayPause.setImageResource(if(mediaPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }


}