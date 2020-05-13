package com.example.lecturerecorder.recorder_activity

import android.media.MediaRecorder
import android.util.Log
import com.example.lecturerecorder.model.NoteResponse
import java.io.IOException
import java.util.*


enum class RecorderState { NOT_STARTED, RECORDING, RECORDED }

private const val LOG_TAG = "RecorderViewModel"

class RecorderViewModel(
    private val fileName: String,
    private val view: RecorderActivity,
    private var notes: MutableList<NoteResponse>
) {
    private var recorder: MediaRecorder? = null
    private var state: RecorderState = RecorderState.NOT_STARTED
    private var timer: Timer? = null
    private var timerStartTime: Calendar? = null

    init {
        view.setButton(ButtonState.RECORD)
    }

    fun onMainButtonPressed() {
        if (state == RecorderState.NOT_STARTED) {
            startRecording()
            startTimer()
            state = RecorderState.RECORDING
            view.setButton(ButtonState.STOP_RECORD)
            view.enableAddNodeButton(true)
        } else if (state == RecorderState.RECORDING) {
            stopRecording()
            stopTimer()
            state = RecorderState.RECORDED
            view.setButton(ButtonState.NULL)
            view.enableAddNodeButton(false)
        }
    }

    fun onStop() {
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null
    }

    fun onSavePressed() {
        view.sendFile()
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null
    }

    private fun startTimer() {
        timer = Timer()
        timerStartTime = Calendar.getInstance()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                view.setTime(getSecondsFromStartTime())
            }

        }, 0, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
        timerStartTime = null
        view.setTime(0)
    }

    fun addNote() {
        notes.add(
            notes.size,
            NoteResponse(
                text = "",
                timestamp = getSecondsFromStartTime().toInt(),
                lectureId = 0,
                picture = ""
            )
        )
        view.showNotesList(notes)
    }

    private fun getSecondsFromStartTime(): Int =
        ((Calendar.getInstance().timeInMillis - timerStartTime?.timeInMillis!!) / 1000).toInt()

    fun onNoteRemove(index: Int) {
        notes.removeAt(index)
        view.showNotesList(notes)
    }

    fun onTextChangedRemove(index: Int, text: String) {
        notes[index].text = text
        // printData()
    }

    private fun printData() {
        Log.d("Niyaz", fileName)

        notes.forEach {
            Log.d("Niyaz", "${it.timestamp}:${it.text}")
        }
    }
}