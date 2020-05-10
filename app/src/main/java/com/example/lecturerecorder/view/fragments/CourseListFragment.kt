package com.example.lecturerecorder.view.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.CourseResponse
import com.example.lecturerecorder.view.adapters.ListAdapter
import com.example.lecturerecorder.model.ListElement
import com.example.lecturerecorder.model.ListElementType
import com.example.lecturerecorder.model.TopicResponse
import com.example.lecturerecorder.utils.RestClient
import com.example.lecturerecorder.utils.SpacedDividerItemDecoration
import com.example.lecturerecorder.utils.parseHttpErrorMessage
import com.example.lecturerecorder.viewmodel.ListViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recycler_list.*

class CourseListFragment : Fragment(), ListAdapter.OnSelectListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var compositeDisposable: CompositeDisposable

    private val model: ListViewModel by activityViewModels()

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

        compositeDisposable = CompositeDisposable()

        viewManager = LinearLayoutManager(activity)
        viewAdapter = ListAdapter(emptyList(), this)

        recyclerView = view.findViewById<RecyclerView>(list_container.id).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        recyclerView.addItemDecoration(
            SpacedDividerItemDecoration(context)
        )

        setActionBarTitle(getString(R.string.courses))

        val selectedTopicId = model.selectedTopicId.value
        if (selectedTopicId != null) {
            loadAndSetData(selectedTopicId)
        }
    }

    fun loadAndSetData(topicId: Int) {
        compositeDisposable.add(
            RestClient.listService.getCourses(topicId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError))
    }

    private fun handleResponse(courses: List<CourseResponse>?) {
        val mappedList = courses?.map{ ListElement(ListElementType.Detailed, it.name, it.description, "${it.audios} ${getString(
            R.string.lectures_underscore)}", it.id)}

        if (mappedList.isNullOrEmpty()) {
            // set empty icon
        } else {
            viewAdapter = ListAdapter(mappedList, this)
            recyclerView.adapter = viewAdapter
            model.courses.postValue(mappedList)
        }
    }

    private fun handleError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
        inflater.inflate(R.menu.appbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        @JvmStatic
        fun newInstance() = CourseListFragment()
    }

    override fun onSelect(position: Int) {
        val elem = model.courses.value?.get(position)?:return
        model.selectedCourseId.postValue(elem.id)
        Toast.makeText(context, elem.title, Toast.LENGTH_LONG).show()
    }
}
