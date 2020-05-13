package com.example.lecturerecorder.view.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.lecturerecorder.R
import com.example.lecturerecorder.contract.NavigationContract
import com.example.lecturerecorder.model.CourseResponse
import com.example.lecturerecorder.model.LectureResponse
import com.example.lecturerecorder.view.adapters.ListAdapter
import com.example.lecturerecorder.model.ListElement
import com.example.lecturerecorder.model.ListElementType
import com.example.lecturerecorder.utils.RestClient
import com.example.lecturerecorder.utils.SpacedDividerItemDecoration
import com.example.lecturerecorder.utils.parseHttpErrorMessage
import com.example.lecturerecorder.view.MainActivity
import com.example.lecturerecorder.viewmodel.ListViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recycler_list.*

class LectureListFragment : Fragment(), ListAdapter.OnSelectListener {


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
        // Inflate the layout for this fragment
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

        setActionBarTitle("Lectures")

        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener {
            (activity as NavigationContract.Container).goToRecorderView(model.selectedCourseId.value!!)
        }

        loadAndSetData()
    }

    fun loadAndSetData() {
        compositeDisposable.add(
            RestClient.listService.getLectures(model.selectedCourseId.value?:return@loadAndSetData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError))
    }

    private fun handleResponse(lectures: List<LectureResponse>?) {
        val mappedList = lectures?.map{ ListElement(ListElementType.Short, it.name, null, null, it.courseId)}

        if (mappedList.isNullOrEmpty()) {
            // set empty icon
            viewAdapter = ListAdapter(emptyList(), this)
            recyclerView.adapter = viewAdapter
            model.lectures.postValue(emptyList())
            showEmptyListIndicator(true)
            setEmptyListText("No lectures available here")
        } else {
            viewAdapter = ListAdapter(mappedList, this)
            recyclerView.adapter = viewAdapter
            model.lectures.postValue(mappedList)
            showEmptyListIndicator(false)
        }
    }

    private fun handleError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // DELETE TOPIC ###########################################################################

    private fun deleteLectureRequest(lectureId: Int) {
        compositeDisposable.add(
            RestClient.listService.deleteLecture(lectureId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::lectureDeleted, this::lectureDeleteError))
    }

    private fun lectureDeleted() {
        Toast.makeText(requireContext(), "Lecture Deleted", Toast.LENGTH_SHORT).show()
        loadAndSetData()
    }


    private fun lectureDeleteError(error: Throwable) {
        Toast.makeText(requireContext(), "Lecture Delete Error", Toast.LENGTH_SHORT).show()
    }

    // UTILS ###########################################################################

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    fun setActionBarTitle(text: String) {
        (activity as AppCompatActivity).supportActionBar?.title = text;
    }

    companion object {
        @JvmStatic
        fun newInstance() = LectureListFragment()
    }

    override fun onSelect(position: Int) {
        val elem = model.lectures.value?.get(position)?:return@onSelect
        (activity as NavigationContract.Container).goToPreviewView(elem.id)
        //Toast.makeText(requireContext(), "Open Preview Activity Here", Toast.LENGTH_LONG).show()
    }

    override fun onLongSelect(position: Int) {
        val elem = model.lectures.value?.get(position)?:return@onLongSelect
        createDeleteConfirmation(elem.id, elem.title)
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

//    private fun createEditDialog(id: Int, name: String, description: String) {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Edit Topic")
//        val innerView: View = LayoutInflater.from(requireContext())
//            .inflate(R.layout.creation_dialog_layout, null)
//
//        val nameField = innerView.findViewById<TextInputLayout>(R.id.name_field)
//        val descrField = innerView.findViewById<TextInputLayout>(R.id.description_field)
//        nameField.editText?.setText(name)
//        descrField.editText?.setText(description)
//
//        builder.setView(innerView)
//        builder.setPositiveButton("Save") { dialog, which ->
//
//            val nt = nameField.editText?.text.toString().trim()
//            val dt = descrField.editText?.text.toString().trim()
//            putTopicRequest(id, nt, dt)
//        }
//
//        builder.setNegativeButton("Delete") {dialog, _->
//            createDeleteConfirmation(id, name)
//            dialog.dismiss()
//        }
//
//        builder.setNeutralButton("Cancel") {_, _->} // do nothing
//        builder.show()
//    }

    private fun createDeleteConfirmation(lectureId: Int, name: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Are you sure?")
        builder.setMessage("Delete $name")
        builder.setPositiveButton("Delete") {_, _->
            deleteLectureRequest(lectureId)
        }
        builder.setNeutralButton("Cancel") {_, _->} // do nothing
        builder.show()
    }
}
