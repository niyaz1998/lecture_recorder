package com.example.lecturerecorder.recorder_activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.lecturerecorder.R
import kotlinx.android.synthetic.main.activity_recorder.*

private const val LOG_TAG = "RecorderActivity"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecorderActivity : AppCompatActivity() {

    private var viewModel: RecorderViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)

        viewModel = RecorderViewModel("${externalCacheDir?.absolutePath}/audiorecordtest.3gp")

        Log.e("recorder path", "${externalCacheDir?.absolutePath}/audiorecordtest.3gp\"")
        bMain.setOnClickListener {
            viewModel?.onMainButtonPressed()
        }

        bAddNote.setOnClickListener {
        }
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
}
