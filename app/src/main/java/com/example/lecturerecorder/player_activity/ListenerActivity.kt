package com.example.lecturerecorder.player_activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.LectureResponse
import com.example.lecturerecorder.model.NoteResponse
import com.example.lecturerecorder.utils.RestClient
import com.example.lecturerecorder.utils.formatTime
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_listener.*


var stubLecture = LectureResponse(
    name = "stub lecture",
    note = listOf(
        NoteResponse(timestamp = 10, text = "note 1", lectureId = 0, picture = "", id = -1),
        NoteResponse(timestamp = 20, text = "note 2", lectureId = 0, picture = "", id = -1),
        NoteResponse(timestamp = 30, text = "note 3", lectureId = 0, picture = "", id = -1),
        NoteResponse(timestamp = 40, text = "note 4", lectureId = 0, picture = "", id = -1),
        NoteResponse(timestamp = 50, text = "note 5", lectureId = 0, picture = "", id = -1),
        NoteResponse(timestamp = 60, text = "note 6", lectureId = 0, picture = "", id = -1)
    ),
    audioFile = "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_2MG.mp3",
    courseId = -1,
    id = -1,
    isOwner = false
)

class ListenerActivity : AppCompatActivity() {

    companion object {
        var ARGUMENTS = "ARGUMENTS"
    }

    lateinit var lectureRecord: LectureResponse
    lateinit var listenerViewModel: ListenerViewModel
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listener)

        compositeDisposable = CompositeDisposable()
        lectureRecord =
            if (intent.extras?.getParcelable<LectureResponse>(ARGUMENTS) != null)
                intent.extras?.getParcelable(ARGUMENTS)!! else stubLecture

        listenerViewModel = ListenerViewModel(
            lectureRecord,
            this,
            seekBar
        )

        compositeDisposable.add(
            RestClient.listService.getNotes(lectureId = lectureRecord.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::notesLoaded, this::notesLoadError)
        )

        bMain.setOnClickListener {
            listenerViewModel.onButtonPressed()
        }
    }

    private fun notesLoaded(response: List<NoteResponse>?) {
        rv_read_notes.apply {
            layoutManager = LinearLayoutManager(this@ListenerActivity)
            adapter =
                ReadNotesAdapter(
                    response ?: emptyList()
                )
        }
    }

    private fun notesLoadError(error: Throwable) {
        Toast.makeText(this, "Notes Load Error", Toast.LENGTH_SHORT).show()
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
        tvTime.text = formatTime(seconds = seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerViewModel.stopPlaying()
        compositeDisposable.clear()
    }

    fun addDroplet(index: Int, relativeMargin: Float) {
        val vi =
            applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = vi.inflate(R.layout.seek_bar_droplet, null)

        v.findViewById<TextView>(R.id.tvIndex).text = index.toString()

        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins((relativeMargin * seekBar.width).toInt(), 0, 0, 0)
        v.layoutParams = params

        droplets.addView(v)
    }
}
