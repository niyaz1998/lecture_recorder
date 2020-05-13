package com.example.lecturerecorder.player_activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.LectureRecord
import com.example.lecturerecorder.model.Note
import com.example.lecturerecorder.model.ReadNotesAdapter
import com.example.lecturerecorder.utils.formatTime
import kotlinx.android.synthetic.main.activity_listener.*

var stubLecture = LectureRecord(
    name = "stub lecture",
    notes = listOf(
        Note(seconds = 10, text = "note 1"),
        Note(seconds = 20, text = "note 2"),
        Note(seconds = 30, text = "note 3"),
        Note(seconds = 40, text = "note 4"),
        Note(seconds = 50, text = "note 5"),
        Note(seconds = 60, text = "note 6")
    ),
    fileLocation = "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_2MG.mp3"
)

class ListenerActivity : AppCompatActivity() {

    companion object {
        var ARGUMENTS = "ARGUMENTS"
    }

    lateinit var lectureRecord: LectureRecord
    lateinit var listenerViewModel: ListenerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listener)

        lectureRecord =
            if (intent.extras?.getParcelable<LectureRecord>(ARGUMENTS) != null)
                intent.extras?.getParcelable(ARGUMENTS)!! else stubLecture

        listenerViewModel = ListenerViewModel(
            lectureRecord,
            this,
            seekBar
        )

        rv_read_notes.apply {
            layoutManager = LinearLayoutManager(this@ListenerActivity)
            adapter = ReadNotesAdapter(lectureRecord.notes)
        }

        bMain.setOnClickListener {
            listenerViewModel.onButtonPressed()
        }
    }

    fun setButtonState(state: MainButtonState) {
        when (state) {
            MainButtonState.PLAYING -> bMain.setImageResource(R.drawable.ic_stop)
            MainButtonState.PAUSED -> bMain.setImageResource(R.drawable.ic_play_circle)
            // MainButtonState.NULL -> bMain.visibility = View.INVISIBLE
            MainButtonState.STOPPED -> bMain.setImageResource(R.drawable.ic_play_circle)
        }
    }

    fun setTime(seconds: Int) {
        tvTime.text = formatTime(seconds = seconds.toLong())
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerViewModel.stopPlaying()
    }
}
