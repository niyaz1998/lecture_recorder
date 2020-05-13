package com.example.lecturerecorder.player_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.Note
import com.example.lecturerecorder.utils.formatTime


class ReadNotesAdapter(
    private val notes: List<Note>
) :
    RecyclerView.Adapter<ReadNotesAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_read_record_note, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tTime.text = formatTime(notes[position].seconds)
        holder.tNoteText.text = notes[position].text
        holder.tIndex.text = (position + 1).toString()
    }

    override fun getItemCount() = notes.size

    class MyViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val tTime: TextView = view.findViewById(R.id.tvTime)
        val tNoteText: TextView = view.findViewById(R.id.tvTextNote)
        val tIndex: TextView = view.findViewById(R.id.tvIndex)
    }
}
