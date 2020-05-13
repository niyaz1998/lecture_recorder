package com.example.lecturerecorder.view.fragments

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.*
import com.example.lecturerecorder.view.adapters.ListAdapter
import com.example.lecturerecorder.utils.RestClient
import com.example.lecturerecorder.utils.SpacedDividerItemDecoration
import com.example.lecturerecorder.utils.parseHttpErrorMessage
import com.example.lecturerecorder.viewmodel.ListViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recycler_list.*
import org.w3c.dom.Text

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
        viewAdapter = ListAdapter(model.lectures.value?: emptyList(), this)

        recyclerView = view.findViewById<RecyclerView>(list_container.id).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        recyclerView.addItemDecoration(
            SpacedDividerItemDecoration(context)
        )

        setActionBarTitle(getString(R.string.courses))

        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener {
            createAdditionDialog()
        }

        loadAndSetData()

    }

    // LOAD LIST DATA ###########################################################################

    fun loadAndSetData() {
        compositeDisposable.add(
            RestClient.listService.getCourses(model.selectedTopicId.value?:return@loadAndSetData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError))
    }

    private fun handleResponse(courses: List<CourseResponse>?) {
        val mappedList = courses?.map{ ListElement(ListElementType.Detailed, it.name, it.description, "${it.audios} ${getString(
            R.string.lectures_underscore)}", it.id)}

        if (mappedList.isNullOrEmpty()) {
            // set empty icon
            viewAdapter = ListAdapter(emptyList(), this)
            recyclerView.adapter = viewAdapter
            model.courses.postValue(emptyList())
            showEmptyListIndicator(true)
            setEmptyListText("No courses available here")
        } else {
            viewAdapter = ListAdapter(mappedList, this)
            recyclerView.adapter = viewAdapter
            model.courses.postValue(mappedList)
            showEmptyListIndicator(false)
        }
    }

    private fun handleError(error: Throwable) {
        //load data from repo
        val message = parseHttpErrorMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // CREATE COURSE ###########################################################################

    private fun createCourseRequest(topicId: Int, name: String, description: String) {
        compositeDisposable.add(
            RestClient.listService.createCourse(topicId, CoursePost(name, description))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::courseCreated, this::courseCreateError))
    }

    private fun courseCreated(response: CourseResponse) {
        Toast.makeText(requireContext(), "Course Created", Toast.LENGTH_SHORT).show()
        loadAndSetData()
    }

    private fun courseCreateError(error: Throwable) {
        Toast.makeText(requireContext(), "Course Creation Error", Toast.LENGTH_SHORT).show()
    }

    // PUT COURSE ###########################################################################

    private fun putCourseRequest(topicId: Int, courseId: Int, name: String, description: String) {
        compositeDisposable.add(
            RestClient.listService.putCourse(topicId, courseId, CoursePost(name, description))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::coursePut, this::coursePutError))
    }

    private fun coursePut(response: CourseResponse) {
        Toast.makeText(requireContext(), "Course Put", Toast.LENGTH_SHORT).show()
        loadAndSetData()
    }

    private fun coursePutError(error: Throwable) {
        Toast.makeText(requireContext(), "Course Put Error", Toast.LENGTH_SHORT).show()
    }

    // DELETE COURSE ###########################################################################

    private fun deleteCourseRequest(topicid: Int, courseId: Int) {
        compositeDisposable.add(
            RestClient.listService.deleteCourse(topicid, courseId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::courseDeleted, this::courseDeleteError))
    }

    private fun courseDeleted() {
        Toast.makeText(requireContext(), "Course Deleted", Toast.LENGTH_SHORT).show()
        loadAndSetData()
    }

    private fun courseDeleteError(error: Throwable) {
        Toast.makeText(requireContext(), "Course Delete Error", Toast.LENGTH_SHORT).show()
    }

    // UTILS ###########################################################################

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
        val elem = model.courses.value?.get(position)?:return@onSelect
        model.selectedCourseId.postValue(elem.id)
        view?.findNavController()?.navigate(R.id.action_courseListFragment_to_lectureListFragment)
    }

    override fun onLongSelect(position: Int) {
        val elem = model.courses.value?.get(position)?:return@onLongSelect
        createEditDialog(model.selectedTopicId.value?:0, elem.id, elem.title, elem.description?:"")
    }


    fun getVisibGone(s: Boolean): Int {
        return if (s) {View.GONE} else {View.VISIBLE}
    }

    private fun showEmptyListIndicator(state: Boolean) {
        requireView().findViewById<RelativeLayout>(R.id.empty_list_info).visibility = getVisibGone(!state)
    }

    private fun setEmptyListText(text: String) {
        requireView().findViewById<TextView>(R.id.empty_list_text).text = text
    }

    private fun createAdditionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("New Course")
        val innerView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.creation_dialog_layout, null)

        builder.setView(innerView)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            val nameField = innerView.findViewById<TextInputLayout>(R.id.name_field)
            val descrField = innerView.findViewById<TextInputLayout>(R.id.description_field)
            val nt = nameField.editText?.text.toString().trim()
            val dt = descrField.editText?.text.toString().trim()
            createCourseRequest(model.selectedTopicId.value!!, nt, dt)
        }

        builder.setNeutralButton("Cancel") {_, _->}
        builder.show()
    }

    private fun createEditDialog(topicId: Int, courseId: Int, name: String, description: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Course")
        val innerView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.creation_dialog_layout, null)

        val nameField = innerView.findViewById<TextInputLayout>(R.id.name_field)
        val descrField = innerView.findViewById<TextInputLayout>(R.id.description_field)
        nameField.editText?.setText(name)
        descrField.editText?.setText(description)

        builder.setView(innerView)
        builder.setPositiveButton("Save") { dialog, which ->

            val nt = nameField.editText?.text.toString().trim()
            val dt = descrField.editText?.text.toString().trim()
            putCourseRequest(topicId, courseId, nt, dt)
        }

        builder.setNegativeButton("Delete") {dialog, _->
            createDeleteConfirmation(topicId, courseId, name)
            dialog.dismiss()
        }

        builder.setNeutralButton("Cancel") {_, _->} // do nothing
        builder.show()
    }

    private fun createDeleteConfirmation(topicId: Int, courseId: Int, name: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Are you sure?")
        builder.setMessage("Delete $name")
        builder.setPositiveButton("Delete") {_, _->
            deleteCourseRequest(topicId, courseId)
        }
        builder.setNeutralButton("Cancel") {_, _->} // do nothing
        builder.show()
    }
}
