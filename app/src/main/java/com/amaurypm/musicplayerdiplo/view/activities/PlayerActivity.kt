package com.amaurypm.musicplayerdiplo.view.activities

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.amaurypm.musicplayerdiplo.R
import com.amaurypm.musicplayerdiplo.databinding.ActivityPlayerBinding
import com.amaurypm.musicplayerdiplo.model.MusicFile
import com.bumptech.glide.Glide


class PlayerActivity : AppCompatActivity(), OnCompletionListener {

    private lateinit var binding: ActivityPlayerBinding

    var position = -1

    var song: MusicFile? = null
    var playing = true
    var isShuffleOn = false
    var isRepeatOn = false

    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras

        if (extras != null) {
            position = extras.getInt("position", -1)
            song = MainActivity.musicFiles[position]
        }

        //Para que el botón de play/pause comience mostrando la imagen de pausa
        binding.fabPlayPause.setImageResource(R.drawable.ic_pause)

        prepareSong(song, true)

        //Para que el usuario pueda mover la seekbar y esto repercuta en la reproducción
        binding.sbSong.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) { //El usuario movió la seekbar
                    mediaPlayer.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }

    fun formattedTime(currentPosition: Int): String {
        var totalOut = ""
        var totalNew = ""
        val seconds = "${currentPosition % 60}"  //10
        val minutes = "${currentPosition / 60}"  //4
        totalOut = "$minutes:$seconds"  //4:3
        totalNew = "$minutes:0$seconds" // 4:03

        return if (seconds.length == 1) totalNew
        else totalOut
    }

    fun setMetaData(song: MusicFile) {
        var cover: ByteArray? = null

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(song.path)
            cover = retriever.embeddedPicture
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (cover != null) {
            Glide.with(this).asBitmap()
                .load(cover)
                .into(binding.ivCover)
        } else {
            Glide.with(this)
                .load(R.drawable.cover)
                .into(binding.ivCover)
        }

        val duration = song.duration?.toInt()?.div(1000)
        binding.tvDuration.text = duration?.let { formattedTime(it) }

    }

    fun prepareSong(song: MusicFile?, start: Boolean) {
        song?.let { song ->
            binding.tvSongName.text = song.title
            binding.tvArtistName.text = song.artist

            mediaPlayer = MediaPlayer.create(this, Uri.parse(song.path))

            mediaPlayer.setOnCompletionListener(this)

            binding.sbSong.max = mediaPlayer.duration / 1000  //como viene en ms, lo paso a segundos
            //Si la canción fuera de 3 minutos, la duración sería de 180000 ms -> 180 s -> 3 minutos
            setMetaData(song)

            runOnUiThread(object : Runnable {
                override fun run() {
                    val currentPosition = mediaPlayer.currentPosition / 1000
                    binding.sbSong.progress = currentPosition
                    binding.tvTime.text = formattedTime(currentPosition)
                    Handler(Looper.myLooper()!!).postDelayed(this, 1000)
                }
            })

            if (start) mediaPlayer.start()

        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mediaPlayer.stop()
        mediaPlayer.release()

        if(isShuffleOn && !isRepeatOn) {
            position = (0 until MainActivity.musicFiles.size).random()
        }else if(!isShuffleOn && !isRepeatOn){
            position = (position+1) % MainActivity.musicFiles.size
        }

        song = MainActivity.musicFiles[position]
        prepareSong(song, true)
        binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
    }

    fun clicks(view: View) {
        when (view.id) {

            R.id.fabPlayPause -> {
                if (mediaPlayer.isPlaying) {
                    binding.fabPlayPause.setImageResource(R.drawable.ic_play)
                    mediaPlayer.pause()
                } else {
                    binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
                    mediaPlayer.start()
                }
            }

            R.id.ivNext -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.release()

                    //Para el Shuffle y el repeat
                    if(isShuffleOn && !isRepeatOn){
                        position = (0 until MainActivity.musicFiles.size).random()
                    }else if(!isShuffleOn && !isRepeatOn){
                        position = (position + 1) % MainActivity.musicFiles.size
                    }

                    //position = (position + 1) % MainActivity.musicFiles.size
                    song = MainActivity.musicFiles[position]
                    prepareSong(song, true)
                    binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
                } else {
                    mediaPlayer.stop()
                    mediaPlayer.release()

                    //Para el shuffle y repeat
                    if(isShuffleOn && !isRepeatOn){
                        position = (0 until MainActivity.musicFiles.size).random()
                    }else if(!isShuffleOn && !isRepeatOn){
                        position = (position + 1) % MainActivity.musicFiles.size
                    }

                    //position = (position + 1) % MainActivity.musicFiles.size
                    song = MainActivity.musicFiles[position]

                    prepareSong(song, false)
                    binding.fabPlayPause.setImageResource(R.drawable.ic_play)
                }
            }

            R.id.ivPrev -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.release()

                    //Para el shuffle y el repeat
                    if(isShuffleOn && !isRepeatOn){
                        position = (0 until MainActivity.musicFiles.size).random()
                    }else if(!isShuffleOn && !isRepeatOn){
                        if ((position - 1) < 0) {
                            position = MainActivity.musicFiles.size - 1
                        } else position -= 1
                    }

                    song = MainActivity.musicFiles[position]
                    prepareSong(song, true)
                    binding.fabPlayPause.setImageResource(R.drawable.ic_pause)
                } else {
                    mediaPlayer.stop()
                    mediaPlayer.release()

                    //Para el shuffle y el repeat
                    if(isShuffleOn && !isRepeatOn){
                        position = (0 until MainActivity.musicFiles.size).random()
                    }else if(!isShuffleOn && !isRepeatOn){
                        if ((position - 1) < 0) {
                            position = MainActivity.musicFiles.size - 1
                        } else position -= 1
                    }

                    /*if ((position - 1) < 0) {
                        position = MainActivity.musicFiles.size - 1
                    } else position -= 1*/

                    song = MainActivity.musicFiles[position]

                    prepareSong(song, false)
                    binding.fabPlayPause.setImageResource(R.drawable.ic_play)
                }
            }

            R.id.ivShuffle -> {
                if (isShuffleOn) { //El usuario desactiva el modo Shuffle que estaba activo
                    isShuffleOn = false
                    binding.ivShuffle.setColorFilter(ContextCompat.getColor(this, R.color.white))
                } else { //El usuario activa el modo shuffle que estaba desactivado
                    isShuffleOn = true
                    isRepeatOn = false
                    binding.ivShuffle.setColorFilter(ContextCompat.getColor(this, R.color.onColor))
                    binding.ivRepeat.setColorFilter(ContextCompat.getColor(this, R.color.white))
                }
            }

            R.id.ivRepeat -> {
                if (isRepeatOn) { //El usuario desactiva el modo Repeat que estaba activo
                    isRepeatOn = false
                    binding.ivRepeat.setColorFilter(ContextCompat.getColor(this, R.color.white))
                } else { //El usuario activa el modo Repeat que estaba desactivado
                    isRepeatOn = true
                    isShuffleOn = false
                    binding.ivRepeat.setColorFilter(ContextCompat.getColor(this, R.color.onColor))
                    binding.ivShuffle.setColorFilter(ContextCompat.getColor(this, R.color.white))
                }
            }

            R.id.ivBack -> {
                finish()
            }
        }
    }



    override fun onRestart() {
        super.onRestart()
        if (playing) {
            mediaPlayer.start()
        }
    }

    override fun onPause() {
        super.onPause()
        playing = mediaPlayer.isPlaying
        mediaPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
    }
}