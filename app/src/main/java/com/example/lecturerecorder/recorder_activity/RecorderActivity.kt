package com.example.lecturerecorder.recorder_activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.NoteResponse
import com.example.lecturerecorder.utils.formatTime
import kotlinx.android.synthetic.main.activity_recorder.*


private const val LOG_TAG = "RecorderActivity"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecorderActivity : AppCompatActivity() {

    private var viewModel: RecorderViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)

        val fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        viewModel = RecorderViewModel(fileName, this, mutableListOf())

        bMain.setOnClickListener {
            viewModel?.onMainButtonPressed()
        }

        bAddNote.setOnClickListener {
            viewModel?.addNote()
        }
        bAddNote.isEnabled = false

        val viewManager = LinearLayoutManager(this)
        val viewAdapter = NotesAdapter(
            emptyList(),
            { viewModel?.onNoteRemove(it) },
            { index, text -> viewModel?.onTextChangedRemove(index, text) }
        )
        rvNotes.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        bSave.setOnClickListener { viewModel?.onSavePressed() }

        ActivityCompat.requestPermissions(
            this,
            permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )
    }

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    override fun onStop() {
        super.onStop()

        viewModel?.onStop()
    }

    fun setTime(seconds: Int) {
        runOnUiThread {
            textTimer.text = formatTime(seconds)
        }
    }

    fun setButton(state: ButtonState) {
        when (state) {
            ButtonState.RECORD -> bMain.setImageResource(R.drawable.ic_radio)
            ButtonState.STOP_RECORD -> bMain.setImageResource(R.drawable.ic_mute)
            ButtonState.NULL -> bMain.visibility = View.INVISIBLE
        }
    }

    fun showNotesList(notes: List<NoteResponse>) {
        val viewAdapter = NotesAdapter(notes,
            { viewModel?.onNoteRemove(it) },
            { index, text -> viewModel?.onTextChangedRemove(index, text) }
        )
        rvNotes.swapAdapter(viewAdapter, false)
    }

    fun enableAddNodeButton(enabled: Boolean) {
        bAddNote.isEnabled = enabled
    }
}

enum class ButtonState { RECORD, STOP_RECORD, NULL }
