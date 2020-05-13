package com.example.lecturerecorder.view.fragments

import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.ListElement
import com.example.lecturerecorder.model.ListElementType
import com.example.lecturerecorder.model.TopicPost
import com.example.lecturerecorder.model.TopicResponse
import com.example.lecturerecorder.utils.RestClient
import com.example.lecturerecorder.utils.SpacedDividerItemDecoration
import com.example.lecturerecorder.utils.parseHttpErrorMessage
import com.example.lecturerecorder.view.adapters.ListAdapter
import com.example.lecturerecorder.viewmodel.ListViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recycler_list.*


class TopicListFragment : Fragment(), ListAdapter.OnSelectListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var compositeDisposable: CompositeDisposable

    private val model: ListViewModel by activityViewModels()

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
        viewAdapter = ListAdapter(model.topics.value?:emptyList(), this)

        recyclerView = view.findViewById<RecyclerView>(list_container.id).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        recyclerView.addItemDecoration(
            SpacedDividerItemDecoration(context)
        )
        setActionBarTitle(getString(R.string.topics))

        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener {
            createAdditionDialog()
        }

        loadAndSetData()
    }

    // LOAD LIST DATA ###########################################################################
    fun loadAndSetData() {
        compositeDisposable.add(
            RestClient.listService.getTopics()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError))
    }

    private fun handleResponse(topics: List<TopicResponse>?) {
        val mappedList = topics?.map{ ListElement(ListElementType.Detailed, it.name, it.description, "${it.courses} ${getString(
            R.string.courses_lowercase)}", it.id)}

        if (mappedList.isNullOrEmpty()) {
            // set empty icon
            viewAdapter = ListAdapter(emptyList(), this)
            recyclerView.adapter = viewAdapter
            model.topics.postValue(emptyList())
            showEmptyListIndicator(true)
            setEmptyListText("No topics available here")
        } else {
            viewAdapter = ListAdapter(mappedList, this)
            recyclerView.adapter = viewAdapter
            model.topics.postValue(mappedList)
            showEmptyListIndicator(false)
        }
    }

    private fun handleError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // CREATE TOPIC ###########################################################################

    private fun createTopicRequest(name: String, description: String) {
        compositeDisposable.add(
            RestClient.listService.createTopic(TopicPost(name, description))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::topicCreated, this::topicCreateError))
    }

    private fun topicCreated(response: TopicResponse) {
        Toast.makeText(requireContext(), "Topic Created", Toast.LENGTH_SHORT).show()
        loadAndSetData()
    }

    private fun topicCreateError(error: Throwable) {
        Toast.makeText(requireContext(), "Topic Creation Error", Toast.LENGTH_SHORT).show()
    }

    // PUT TOPIC ###########################################################################

    private fun putTopicRequest(id: Int, name: String, description: String) {
        compositeDisposable.add(
            RestClient.listService.putTopic(id, TopicPost(name, description))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::topicPut, this::topicPutError))
    }

    private fun topicPut(reponse: TopicResponse) {
        Toast.makeText(requireContext(), "Topic Put", Toast.LENGTH_SHORT).show()
        loadAndSetData()
    }

    private fun topicPutError(error: Throwable) {
        Toast.makeText(requireContext(), "Topic Put Error", Toast.LENGTH_SHORT).show()
    }

    // DELETE TOPIC ###########################################################################

    private fun deleteTopicRequest(id: Int) {
        compositeDisposable.add(
            RestClient.listService.deleteTopic(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::topicDeleted, this::topicDeleteError))
    }

    private fun topicDeleted() {
        Toast.makeText(requireContext(), "Topic Deleted", Toast.LENGTH_SHORT).show()
        loadAndSetData()
    }

    private fun topicDeleteError(error: Throwable) {
        Toast.makeText(requireContext(), "Topic Delete Error", Toast.LENGTH_SHORT).show()
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
        val elem = model.topics.value?.get(position)?:return@onSelect
        model.selectedTopicId.postValue(elem.id)
        view?.findNavController()?.navigate(R.id.action_topicListFragment_to_courseListFragment)
    }

    override fun onLongSelect(position: Int) {
        val elem = model.topics.value?.get(position)?:return@onLongSelect
        createEditDialog(elem.id, elem.title, elem.description?:"")
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
        builder.setTitle("New Topic")
        val innerView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.creation_dialog_layout, null)

        builder.setView(innerView)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            val nameField = innerView.findViewById<TextInputLayout>(R.id.name_field)
            val descrField = innerView.findViewById<TextInputLayout>(R.id.description_field)
            val nt = nameField.editText?.text.toString().trim()
            val dt = descrField.editText?.text.toString().trim()
            createTopicRequest(nt, dt)
        }

        builder.setNeutralButton("Cancel") {_, _->}
        builder.show()
    }

    private fun createEditDialog(id: Int, name: String, description: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Topic")
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
            putTopicRequest(id, nt, dt)
        }

        builder.setNegativeButton("Delete") {dialog, _->
            createDeleteConfirmation(id, name)
            dialog.dismiss()
        }

        builder.setNeutralButton("Cancel") {_, _->} // do nothing
        builder.show()
    }

    private fun createDeleteConfirmation(id: Int, name: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Are you sure?")
        builder.setMessage("Delete $name")
        builder.setPositiveButton("Delete") {_, _->
            deleteTopicRequest(id)
        }
        builder.setNeutralButton("Cancel") {_, _->} // do nothing
        builder.show()
    }
}
