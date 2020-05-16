package com.example.lecturerecorder.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.ListElement
import com.example.lecturerecorder.model.ListElementType

class ListAdapter(
    private val list: List<ListElement>,
    private val onSelectListener: OnSelectListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnSelectListener {
        fun onSelect(position: Int)
        fun onLongSelect(position: Int)
    }

    interface ListElementViewHolder {
        fun bind(element: ListElement)
    }

    class DetailedViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        listener: OnSelectListener
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.list_element_detailed, parent, false
        )
    ), View.OnClickListener, View.OnLongClickListener, ListElementViewHolder {
        private var mTitleView: TextView? = null
        private var mDescriptionView: TextView? = null
        private var mInfoView: TextView? = null
        private var onSelectListener: OnSelectListener? = null

        init {
            mTitleView = itemView.findViewById(R.id.list_title)
            mDescriptionView = itemView.findViewById(R.id.list_description)
            mInfoView = itemView.findViewById(R.id.list_info)
            onSelectListener = listener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun bind(element: ListElement) {
            mTitleView?.text = element.title
            mDescriptionView?.text = element.description
            mInfoView?.text = element.info
        }

        override fun onClick(v: View?) {
            onSelectListener?.onSelect(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            onSelectListener?.onLongSelect(adapterPosition)
            return true
        }
    }

    class ShortViewHolder(inflater: LayoutInflater, parent: ViewGroup, listener: OnSelectListener) :
        RecyclerView.ViewHolder(
            inflater.inflate(
                R.layout.list_element_short, parent, false
            )
        ), View.OnClickListener, View.OnLongClickListener, ListElementViewHolder {
        private var mTitleView: TextView? = null
        private var onSelectListener: OnSelectListener? = null

        init {
            mTitleView = itemView.findViewById(R.id.list_title)
            onSelectListener = listener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun bind(element: ListElement) {
            mTitleView?.text = element.title
        }

        override fun onClick(v: View?) {
            onSelectListener?.onSelect(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            onSelectListener?.onLongSelect(adapterPosition)
            return true
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ListElementType.Detailed.ordinal -> DetailedViewHolder(
                inflater,
                parent,
                onSelectListener
            )
            else -> ShortViewHolder(inflater, parent, onSelectListener)
        }
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListElementViewHolder).bind(list[position])
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type.ordinal
    }
}