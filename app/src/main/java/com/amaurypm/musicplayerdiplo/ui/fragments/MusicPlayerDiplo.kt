package com.amaurypm.musicplayerdiplo.ui.fragments

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.data.AudioRepository
import com.amaurypm.musicplayerdiplo.data.local.model.MusicFile
import com.amaurypm.musicplayerdiplo.databinding.FragmentMusicPlayerDiploBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayerDiplo : Fragment(), MediaPlayer.OnCompletionListener, View.OnClickListener {

    private var _binding: FragmentMusicPlayerDiploBinding? = null
    private val binding get() = _binding!!

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
        // Inflate the layout for this fragment
        _binding = FragmentMusicPlayerDiploBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Recuperamos los argumentos o parámetros recibidos
        val args: MusicPlayerDiploArgs by navArgs()

        position = args.position

        musicViewModel.getAllAudio()

        musicViewModel.musicFiles.observe(viewLifecycleOwner){ songs ->
            audioList = songs  //Listado total de canciones
            song = songs[position] //Canción seleccionada por el usuario

            prepareSong(song, true)

            //Ponemos de inicio el botón de pausa
            binding.fabPlayPause.setImageResource(R.drawable.ic_pause)

            //Establecemos el manejo manual de la seekbar
            binding.sbSong.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int, //viene en segundos
                    fromUser: Boolean
                ) {
                    //Revisamos si el que la movió fue el usuario
                    if(fromUser){
                        mediaPlayer.seekTo(progress*1000) //lo regreso a milisegundos (así lo requiere el mediaplayer)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            })



            binding.apply {
                fabPlayPause.setOnClickListener(this@MusicPlayerDiplo)
                ivNext.setOnClickListener(this@MusicPlayerDiplo)
                ivPrev.setOnClickListener(this@MusicPlayerDiplo)
                ivShuffle.setOnClickListener(this@MusicPlayerDiplo)
                ivRepeat.setOnClickListener(this@MusicPlayerDiplo)
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    private fun setMetaData(song: MusicFile){

        //Obtenemos la imagen de la canción si la tiene
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
            //Manejamos la excepción
            e.printStackTrace()
        }

        Glide.with(requireContext()).asBitmap()
            .load(cover)
            .error(R.drawable.cover)
            .into(binding.ivCover)

        binding.apply {
            tvDuration.text = formattedTime(song.duration.toInt())
            tvSongName.text = song.title
            tvArtistName.text = song.artist
        }

    }

    private fun formattedTime(currentMilisPosition: Int): String{
        val currentPosition = currentMilisPosition / 1000  //Lo pasamos a segundos
        var totalOut = ""
        var totalNew = ""
        val seconds  = "${currentPosition%60}"
        val minutes  = "${currentPosition/60}"
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"

        return if(seconds.length == 1) totalNew
        else totalOut

        //Si le pasamos 198000ms -> 198s -> 3:18
        //Si le pasamos 5000ms -> 5s -> 0:05
    }

    //Función que se llama cuando el contenido se termina de reproducir
    override fun onCompletion(mp: MediaPlayer?) {
        mediaPlayer.release()
        updatePosition(isNext = true)
        song = audioList[position]
        prepareSong(song, true)
        binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
    }

    private fun prepareSong(selectedSong: MusicFile?, start: Boolean){
        selectedSong?.let { song ->
            setMetaData(song)

            //Instanciamos el objeto del mediaplayer con la canción a reproducir
            mediaPlayer = MediaPlayer.create(requireContext(), song.data.toUri())

            mediaPlayer.setOnCompletionListener(this)

            binding.sbSong.max = mediaPlayer.duration/1000

            //Actualizando la seekbar con una corrutina:
            lifecycleScope.launch(Dispatchers.Main) {
                while(isActive){
                    binding.sbSong.progress = mediaPlayer.currentPosition/1000
                    binding.tvTime.text = formattedTime(mediaPlayer.currentPosition)
                    delay(1000)
                }
            }

            //Reproducimos el contenido si la bandera viene en true
            if(start){
                mediaPlayer.start()
                playing = mediaPlayer.isPlaying
            }
        }
    }

    //Para tener concentrados los clicks a las vistas del fragment
    override fun onClick(view: View?) {
        when(view){
            binding.fabPlayPause -> togglePlayPause()
            binding.ivNext -> playNextOrPrevious(isNext = true)
            binding.ivPrev -> playNextOrPrevious(isNext = false)
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

    //Para actualizar la posición en todos los casos (shuffle, repeat, normal)
    private fun updatePosition(isNext: Boolean){
        position = when{
            //Shuffle está encendido
            isShuffleOn && !isRepeatOn -> (0 until audioList.size).random()
            //Modo normal
            !isShuffleOn && !isRepeatOn ->
                if(isNext) (position+1) % audioList.size else (position-1 + audioList.size) % audioList.size
            //Repeat encendido
            else -> position
            // 10  0 al 9   9    10%10 0  3 -> 4%10 = 4
            // 10  0 al 9   -1+10 = 9   9%10 ->  9      3-1 = 2+10 = 12   12%10 = 2
        }
    }

    private fun playNextOrPrevious(isNext: Boolean){
        //Actualizo la posición
        updatePosition(isNext)
        //Cargo la nueva canción
        song = audioList[position]
        //Reviso si se estaba reproduciendo
        playing = mediaPlayer.isPlaying
        //Libero el mediaplayer
        mediaPlayer.release()
        //Preparo la nueva canción
        prepareSong(song, playing)
        //Actualizo el botón play/pause
        binding.fabPlayPause.setImageResource(
            if(mediaPlayer.isPlaying)
                R.drawable.ic_pause
            else
                R.drawable.ic_play
        )
    }

    private fun toggleShuffle(){
        isShuffleOn = !isShuffleOn
        if(isShuffleOn){ //Encendemos el modo shuffle
            isRepeatOn = false
            binding.ivShuffle.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.onColor)
            )
            binding.ivRepeat.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }else{
            binding.ivShuffle.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }
    }

    private fun toggleRepeat(){
        isRepeatOn = !isRepeatOn
        if(isRepeatOn){ //Encendemos el modo repeat
            isShuffleOn = false
            binding.ivRepeat.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.onColor)
            )
            binding.ivShuffle.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }else{
            binding.ivRepeat.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }
    }


}