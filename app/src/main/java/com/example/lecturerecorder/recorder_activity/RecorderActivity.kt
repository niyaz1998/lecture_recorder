package com.example.lecturerecorder.recorder_activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.NoteResponse
import com.example.lecturerecorder.utils.AudioUploadService
import com.example.lecturerecorder.utils.formatTime
import kotlinx.android.synthetic.main.activity_recorder.*
import java.util.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecorderActivity : AppCompatActivity() {

    private var viewModel: RecorderViewModel? = null
    private var courseId: Int? = null
    private lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)

        courseId = intent.extras?.getInt("courseId")

        val name = "audio_${courseId}_${Calendar.getInstance().timeInMillis}"
        fileName = "${externalCacheDir?.absolutePath}/$name.3gp"

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

        bSave.setOnClickListener { viewModel?.onSavePressed(etLectureName.text.toString()) }

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
            // ButtonState.UPLOAD_FILE -> bMain.setImageResource(R.drawable.ic_file_upload)
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

    fun sendFile(
        lectureName: String,
        notes: MutableList<NoteResponse>
    ) {
        val mIntent = Intent(this, AudioUploadService::class.java)
        mIntent.putExtra("mFilePath", fileName)
        mIntent.putExtra("courseId", courseId)
        mIntent.putExtra("name", lectureName)
        mIntent.putParcelableArrayListExtra("notes", ArrayList(notes))
        AudioUploadService.enqueueWork(this, mIntent)
    }

    fun enableSubmitButton() {
        bSave.isEnabled = true
    }

    fun checkLectureName(): Boolean {
        val result = etLectureName.text.isNotEmpty()
        if (!result) {
            val toast = Toast.makeText(
                applicationContext,
                resources.getString(R.string.enter_lecture_name), Toast.LENGTH_SHORT
            )
            toast.show()
        }
        return result;
    }

    fun stop() {
        onBackPressed()
    }
}

enum class ButtonState { RECORD, STOP_RECORD, NULL }
