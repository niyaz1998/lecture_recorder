package com.example.lecturerecorder.recorder_activity

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.NoteResponse
import com.example.lecturerecorder.utils.formatTime


class NotesAdapter(
    private val myDataset: List<NoteResponse>,
    private val onRemovePressed: (index: Int) -> Unit,
    private val onTextChanged: (index: Int, text: String) -> Unit
) :
    RecyclerView.Adapter<NotesAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_record_note, parent, false),
            MyTextWatcher(onTextChanged)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val index = myDataset.size - position - 1

        holder.tTime.text = formatTime(myDataset[index].timestamp)
        holder.bRemove.setOnClickListener { onRemovePressed(index) }

        holder.textWatcher.updatePosition(index)
        holder.etNote.setText(myDataset[index].text)
    }

    override fun getItemCount() = myDataset.size

    class MyViewHolder(view: View, val textWatcher: MyTextWatcher) :
        RecyclerView.ViewHolder(view) {
        val tTime: TextView = view.findViewById(R.id.tTime)
        val etNote: EditText = view.findViewById(R.id.etNote)
        val bRemove: ImageView = view.findViewById(R.id.iRemoveIcon)

        init {
            etNote.addTextChangedListener(textWatcher)
        }
    }
}

class MyTextWatcher(
    private val onTextChanged: (index: Int, text: String) -> Unit
) : TextWatcher {

    var position: Int? = null

    fun updatePosition(position: Int) {
        this.position = position
    }

    override fun afterTextChanged(s: Editable?) {
        position?.let { onTextChanged(it, s.toString()) }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

}
