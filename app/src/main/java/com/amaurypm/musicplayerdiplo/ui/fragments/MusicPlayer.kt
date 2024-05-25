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
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.data.local.model.AudioRepository
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import com.amaurypm.musicplayerdiplo.databinding.FragmentMusicPlayerBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayer : Fragment(), OnCompletionListener, View.OnClickListener {

    private var _binding: FragmentMusicPlayerBinding? = null
    private val binding get() = _binding!!

    private val musicListViewModel: MusicListViewModel by viewModels {
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
            //Obtenemos el listado de canciones y la canción a reproducir
            audioList = songs.toMutableList()
            song = songs[position]

            binding.fabPlayPause.setImageResource(R.drawable.ic_pause)

            prepareSong(song, true)

            binding.sbSong.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                        //Si el usuario hizo un gesto para moverla
                        if(fromUser){
                            //Lo regreso a milisegundos, porque el mediaplayer utiliza ms
                            mediaPlayer.seekTo(progress*1000)
                        }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            })

            binding.ivNext.setOnClickListener(this)
            binding.ivPrev.setOnClickListener(this)
            binding.fabPlayPause.setOnClickListener(this)
            binding.ivShuffle.setOnClickListener(this)
            binding.ivRepeat.setOnClickListener(this)

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mediaPlayer.release()
    }

    private fun formattedTime(currentPosition: Int): String{

        //20
        //0:20

        //62
        //1:02

        //195
        //3:15

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
            retriever.setDataSource(song.path)
            cover = retriever.embeddedPicture

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                retriever.close()
            else
                retriever.release()
        }catch(e: Exception){
            //Manejo de la excepción
            e.printStackTrace()
        }

        if(cover != null){
            Glide.with(requireContext()).asBitmap()
                .load(cover)
                .into(binding.ivCover)
        }else{
            binding.ivCover.setImageResource(R.drawable.cover)
        }

        val duration = song.duration.toInt()/1000  //Pasamos la duración a segundos (viene en milisegundos)
        binding.tvDuration.text = formattedTime(duration)

    }

    //Método para manejar cuando un contenido se termina de reproducir
    override fun onCompletion(mp: MediaPlayer?) {
        mediaPlayer.release()
        updatePosition(isNext = true)
        song = audioList[position]
        prepareSong(song, true)
        binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
    }

    //Función para tener lista una canción para reproducción
    //start es un parámetro para ver si reproducimos directamente la canción o no
    private fun prepareSong(selectedSong: MusicFile?, start: Boolean){
        selectedSong?.let{ song ->
            binding.tvSongName.text = song.title
            binding.tvArtistName.text = song.artist

            mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(song.path))

            mediaPlayer.setOnCompletionListener(this)

            binding.sbSong.max = mediaPlayer.duration/1000

            setMetaData(song)

            //Para actualizar la seekbar cada segundo
            //vamos a usar una corrutina

            lifecycleScope.launch(Dispatchers.Main) {
                while (isActive){
                    val currentPosition = mediaPlayer.currentPosition/1000
                    binding.sbSong.progress = currentPosition
                    binding.tvTime.text = formattedTime(currentPosition)
                    delay(1000)
                }
            }

            if(start){
                mediaPlayer.start()
                playing = mediaPlayer.isPlaying
            }

        }
    }

    override fun onStart() {
        super.onStart()
        if(playing){
            mediaPlayer.start()
        }
    }

    override fun onPause() {
        super.onPause()
        playing = mediaPlayer.isPlaying
        mediaPlayer.pause()
    }

    override fun onClick(view: View?) {
        //Aquí voy a poder manejar todos los clicks a las vistas
        when(view){
            binding.fabPlayPause -> togglePlayPause()
            binding.ivNext -> playNextOrPrevious(true)
            binding.ivPrev -> playNextOrPrevious(false)
            binding.ivShuffle -> toggleShuffle()
            binding.ivRepeat -> toggleRepeat()
        }
    }

    private fun togglePlayPause(){
        if(mediaPlayer.isPlaying){
            binding.fabPlayPause.setImageResource(R.drawable.ic_play)
            mediaPlayer.pause()
        }else{
            binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
            mediaPlayer.start()
        }
    }

    //isNext es para saber si vamos hacia adelante o hacia atrás
    private fun updatePosition(isNext: Boolean){
        position = when{
            //Shuffle está encendido
            isShuffleOn && !isRepeatOn -> (0 until audioList.size).random()
            //Normal (sin shuffle y sin repeat)
            !isShuffleOn && !isRepeatOn ->
                if(isNext)(position + 1) % audioList.size
                else (position - 1 + audioList.size) % audioList.size
            //El Repeat está encendido
            else -> position
        }
    }

    private fun playNextOrPrevious(isNext: Boolean){
        updatePosition(isNext)
        song = audioList[position]
        playing = mediaPlayer.isPlaying
        mediaPlayer.release()
        prepareSong(song, playing)
        binding.fabPlayPause.setImageResource(
            if(mediaPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun toggleShuffle(){
        isShuffleOn = !isShuffleOn
        if(isShuffleOn){
            isRepeatOn = false
            binding.ivShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.onColor))
            binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        }else{
            binding.ivShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun toggleRepeat(){
        isRepeatOn = !isRepeatOn
        if(isRepeatOn){
            isShuffleOn = false
            binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.onColor))
            binding.ivShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        }else{
            binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }



}