package com.example.lecturerecorder.player_activity

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import com.example.lecturerecorder.model.LectureRecord

class ListenerViewModel(
    var lectureRecord: LectureRecord,
    var listenerActivity: ListenerActivity,
    var mSeekBar: SeekBar
) {
    var mPlayer: MediaPlayer? = null
    private val mHandler: Handler = Handler()
    private var mRunnable: Runnable? = null
    private var buttonState: MainButtonState = MainButtonState.STOPPED

    init {
        initSeekBarListener()
        getAudioStats()
    }

    fun onButtonPressed() {
        when (buttonState) {
            MainButtonState.STOPPED -> {
                stopPlaying()
                initMediaPlayer()
                initializeSeekBar()
                continuePlaying()
            }
            MainButtonState.PLAYING -> {
                pausePlaying()
            }
            MainButtonState.PAUSED -> {
                continuePlaying()
            }
            // MainButtonState.NULL -> { }
        }
        listenerActivity.setButtonState(buttonState)
    }

    private fun setButtonState(state: MainButtonState) {
        buttonState = state
        listenerActivity.setButtonState(state)
    }

    private fun continuePlaying() {
        mPlayer?.start()
        setButtonState(MainButtonState.PLAYING)
    }

    private fun pausePlaying() {
        mPlayer?.pause()
        setButtonState(MainButtonState.PAUSED)
    }

    fun stopPlaying() {
        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable!!)
        }
        setButtonState(MainButtonState.STOPPED)
    }

    private fun initSeekBarListener() {
        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mPlayer?.seekTo(progress * 1000)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initMediaPlayer() {
        mPlayer = MediaPlayer()
        mPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mPlayer!!.setDataSource(lectureRecord.fileLocation)
            mPlayer!!.prepare()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun initializeSeekBar() {
        mSeekBar.max = mPlayer!!.duration / 1000

        mRunnable = Runnable {
            if (mPlayer != null) {
                val mCurrentPosition = mPlayer!!.currentPosition / 1000
                mSeekBar.progress = mCurrentPosition
                getAudioStats()
            }
            mHandler.postDelayed(mRunnable!!, 1000)
        }
        mHandler.postDelayed(mRunnable!!, 1000)


        for (i in lectureRecord.notes.indices) {
            listenerActivity.addDroplet(
                i + 1,
                lectureRecord.notes[i].timestamp.toFloat() / mSeekBar.max.toFloat()
            )
        }
    }

    private fun getAudioStats() {
        if (mPlayer != null) {
            // val duration = mPlayer!!.duration / 1000
            val due = (mPlayer!!.duration - mPlayer!!.currentPosition) / 1000
            // val pass = duration - due

            if (due == 0) {
                stopPlaying()
            }
            listenerActivity.setTime(due)
        } else {
            listenerActivity.setTime(0)
        }
    }
}

enum class MainButtonState {
    STOPPED,
    PLAYING,
    PAUSED
}