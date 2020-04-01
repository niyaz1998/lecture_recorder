package com.example.lecturerecorder.view.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.lecturerecorder.R
import com.example.lecturerecorder.adapters.ListAdapter
import com.example.lecturerecorder.model.ListElement
import com.example.lecturerecorder.model.ListElementType
import com.example.lecturerecorder.utils.SpacedDividerItemDecoration
import kotlinx.android.synthetic.main.fragment_recycler_list.*

class CourseListFragment : Fragment(), ListAdapter.OnSelectListener {

    private val testData = listOf(
        ListElement(ListElementType.Detailed,"Topic 1", "some description 1", "info 1", "id1"),
        ListElement(ListElementType.Short,"Topic 2", "some description 2", "info 1", "id2"),
        ListElement(ListElementType.Detailed,"Topic 3", "some description 3", "info 1", "id3"),
        ListElement(ListElementType.Short,"Topic 4", "some description 4", "info 1", "id4"),
        ListElement(ListElementType.Short,"Topic 5", "some description 5", "info 1", "id5"),
        ListElement(ListElementType.Detailed,"Topic 6", "some description 6", "info 1", "id6"),
        ListElement(ListElementType.Detailed,"Topic 7", "some description 7", "info 1", "id7")
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recycler_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewManager = LinearLayoutManager(activity)
        viewAdapter = ListAdapter(testData, this)

        recyclerView = view.findViewById<RecyclerView>(list_container.id).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        recyclerView.addItemDecoration(
            SpacedDividerItemDecoration(context)
        )
        setActionBarTitle("Courses")
    }

    fun setActionBarTitle(text: String) {
        (activity as AppCompatActivity).supportActionBar?.title = text;
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.appbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        @JvmStatic
        fun newInstance() = CourseListFragment()
    }

    override fun onSelect(position: Int) {
        val elem = testData[position]
        Toast.makeText(context, elem.title, Toast.LENGTH_LONG).show()
    }
}
