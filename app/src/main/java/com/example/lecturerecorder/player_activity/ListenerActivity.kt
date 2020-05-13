package com.example.lecturerecorder.player_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.LectureRecord
import com.example.lecturerecorder.model.Note
import com.example.lecturerecorder.model.ReadNotesAdapter
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
    fileLocation = ""
)

class ListenerActivity : AppCompatActivity() {

    companion object {
        var ARGUMENTS = "ARGUMENTS"
    }

    lateinit var lectureRecord: LectureRecord

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listener)

        lectureRecord =
            if (intent.extras?.getParcelable<LectureRecord>(ARGUMENTS) != null)
                intent.extras?.getParcelable(ARGUMENTS)!! else stubLecture

        rv_read_notes.apply {
            layoutManager = LinearLayoutManager(this@ListenerActivity)
            adapter = ReadNotesAdapter(lectureRecord.notes)
        }
    }
}
