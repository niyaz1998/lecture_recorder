package com.example.lecturerecorder.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lecturerecorder.R
import com.example.lecturerecorder.view.adapters.ListAdapter
import com.example.lecturerecorder.model.ListElement
import com.example.lecturerecorder.model.ListElementType
import com.example.lecturerecorder.model.Topic
import com.example.lecturerecorder.utils.RestClient
import com.example.lecturerecorder.utils.SpacedDividerItemDecoration
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recycler_list.*


class TopicListFragment : Fragment(), ListAdapter.OnSelectListener {

    private var testData = listOf(
        ListElement(ListElementType.Detailed,"Topic 1", "some description 1", "12 courses", "id1"),
        ListElement(ListElementType.Short,"Course", "some description 2", "info 1", "id2"),
        ListElement(ListElementType.Detailed,"Topic 3", "some description 3", "7 courses", "id3"),
        ListElement(ListElementType.Short,"Lecture 7", "some description 4", "info 1", "id4"),
        ListElement(ListElementType.Short,"Lectue 8", "some description 5", "info 1", "id5"),
        ListElement(ListElementType.Detailed,"Topic 6", "some description 6", "2 courses", "id6"),
        ListElement(ListElementType.Detailed,"Topic 7", "some description 7", "3 courses", "id7")
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var compositeDisposable: CompositeDisposable

    // TODO pass list data via viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recycler_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        compositeDisposable = CompositeDisposable()

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
        setActionBarTitle("Topics") // TODO: use string resources
        // TODO: set viewmodel to observe data

        loadAndSetData()
    }

    fun loadAndSetData() {
        compositeDisposable.add(
            RestClient.listService.getTopics()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError))
    }

    private fun handleResponse(topics: List<Topic>) {
        testData = topics.map{ ListElement(ListElementType.Detailed, it.name, it.description, it.courses.toString()+" courses", it.id.toString())}
        viewAdapter = ListAdapter(testData, this)
        recyclerView.adapter = viewAdapter
    }

    private fun handleError(error: Throwable) {
        //Log.d(TAG, error.localizedMessage)
        Toast.makeText(context, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    fun setActionBarTitle(text: String) {
        (activity as AppCompatActivity).supportActionBar?.title = text;
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.topic_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
//        menu.clear() // clears all menu items..
//        activity!!.menuInflater.inflate(R.menu.fragment_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater!!)
//    }

    companion object {
        @JvmStatic
        fun newInstance() = TopicListFragment()
    }

    override fun onSelect(position: Int) {
        val elem = testData[position]
        view?.findNavController()?.navigate(R.id.action_topicListFragment_to_courseListFragment)
        // modify titlebar
        //(activity as )?.setTitleBarText("Course: ")
    }
}
