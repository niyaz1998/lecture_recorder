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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.lecturerecorder.R
import com.example.lecturerecorder.contract.NavigationContract
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recycler_list.*
import kotlinx.android.synthetic.main.fragment_recycler_list.view.*


class TopicListFragment : Fragment(), ListAdapter.OnSelectListener, NavigationContract.Fragment {

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

        // setup recycler view
        viewManager = LinearLayoutManager(activity)
        viewAdapter = ListAdapter(model.topics.value ?: emptyList(), this)
        recyclerView = view.findViewById<RecyclerView>(list_container.id).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        recyclerView.addItemDecoration(
            SpacedDividerItemDecoration(context)
        )

        // setup appbar
        setActionBarTitle(getString(R.string.topics))
        (activity as NavigationContract.Container).enableBackButton(false)

        // setup bottom floating button
        fab_add?.setOnClickListener {
            createAdditionDialog()
        }

        // setup header under appbar
        (activity as NavigationContract.Container).setHeaderVisibility(false)

        // setup swipe refresh
        requireView().swiperefresh?.setOnRefreshListener {
            loadAndSetData()
        }

        // override back button actions
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.use_home_button_to_exit),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
                true
            } else {
                false
            }
        }

        // update data
        loadAndSetData()
    }

    // LOAD LIST DATA ###########################################################################

    private fun loadAndSetData() {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.getTopics()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError)
        )
    }

    private fun handleResponse(topics: List<TopicResponse>?) {
        val mappedList = topics?.map {
            ListElement(
                ListElementType.Detailed,
                if (it.isOwner) {
                    "${it.name} ${getString(R.string.owned_addition_text)}"
                } else {
                    it.name
                },
                it.description, "${it.courses} ${getString(
                    R.string.courses_lowercase
                )}",
                it.id, it.isOwner
            )
        }

        if (mappedList.isNullOrEmpty()) {
            // set empty icon
            viewAdapter = ListAdapter(emptyList(), this)
            recyclerView.adapter = viewAdapter
            model.topics.postValue(emptyList())
            showEmptyListIndicator(true)
            setEmptyListText(getString(R.string.no_topics_available_here))
        } else {
            viewAdapter = ListAdapter(mappedList, this)
            recyclerView.adapter = viewAdapter
            model.topics.postValue(mappedList)
            showEmptyListIndicator(false)
        }
        swiperefresh?.isRefreshing = false
    }

    private fun handleError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        swiperefresh?.isRefreshing = false
    }

    // CREATE TOPIC ###########################################################################

    private fun createTopicRequest(name: String, description: String) {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.createTopic(TopicPost(name, description))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::topicCreated, this::topicCreateError)
        )
    }

    private fun topicCreated(response: TopicResponse) {
        Snackbar.make(requireView(), getString(R.string.topic_created), Snackbar.LENGTH_SHORT)
            .show()
        loadAndSetData()
    }

    private fun topicCreateError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .show()
        swiperefresh?.isRefreshing = false
    }

    // PUT TOPIC ###########################################################################

    private fun putTopicRequest(id: Int, name: String, description: String) {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.putTopic(id, TopicPost(name, description))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::topicPut, this::topicPutError)
        )
    }

    private fun topicPut(response: TopicResponse) {
        Snackbar.make(requireView(), getString(R.string.topic_updated), Snackbar.LENGTH_SHORT)
            .show()
        loadAndSetData()
    }

    private fun topicPutError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .show()
        swiperefresh?.isRefreshing = false
    }

    // DELETE TOPIC ###########################################################################

    private fun deleteTopicRequest(id: Int) {
        requireView().swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.deleteTopic(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::topicDeleted, this::topicDeleteError)
        )
    }

    private fun topicDeleted() {
        Snackbar.make(requireView(), getString(R.string.topic_deleted), Snackbar.LENGTH_SHORT)
            .show()
        loadAndSetData()
    }

    private fun topicDeleteError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .show()
        swiperefresh?.isRefreshing = false
    }

    // UTILS ###########################################################################

    override fun onDestroy() {
        if (this::compositeDisposable.isInitialized) {
            compositeDisposable.clear()
        }
        super.onDestroy()
    }

    private fun setActionBarTitle(text: String) {
        (activity as AppCompatActivity).supportActionBar?.title = text;
    }

    companion object {
        @JvmStatic
        fun newInstance() = TopicListFragment()
    }

    override fun onSelect(position: Int) {
        val elem = model.topics.value?.get(position) ?: return@onSelect
        model.selectedTopicId.postValue(elem.id)
        model.selectedTopicName.postValue(elem.title)
        model.isTopicOwned.postValue(elem.isEditable)
        view?.findNavController()?.navigate(R.id.action_topicListFragment_to_courseListFragment)
    }

    override fun onLongSelect(position: Int) {
        val elem = model.topics.value?.get(position) ?: return@onLongSelect
        if (elem.isEditable) {
            createEditDialog(elem.id, elem.title, elem.description ?: "")
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.this_is_not_your_topic),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getVisibGone(s: Boolean): Int {
        return if (s) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showEmptyListIndicator(state: Boolean) {
        requireView().empty_list_info?.visibility = getVisibGone(!state)
    }

    private fun setEmptyListText(text: String) {
        requireView().empty_list_text?.text = text
    }

    private fun createAdditionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.new_topic))
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

        builder.setNeutralButton(getString(R.string.cancel_cap)) { _, _ -> }
        builder.show()
    }

    private fun createEditDialog(id: Int, name: String, description: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.edit_topic))
        val innerView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.creation_dialog_layout, null)

        val nameField = innerView.findViewById<TextInputLayout>(R.id.name_field)
        val descrField = innerView.findViewById<TextInputLayout>(R.id.description_field)
        nameField.editText?.setText(name.dropLast(5))
        descrField.editText?.setText(description)

        builder.setView(innerView)
        builder.setPositiveButton(getString(R.string.save_cap)) { dialog, which ->

            val nt = nameField.editText?.text.toString().trim()
            val dt = descrField.editText?.text.toString().trim()
            putTopicRequest(id, nt, dt)
        }

        builder.setNegativeButton(getString(R.string.delete_cap)) { dialog, _ ->
            createDeleteConfirmation(id, name)
            dialog.dismiss()
        }

        builder.setNeutralButton(getString(R.string.cancel_cap)) { _, _ -> } // do nothing
        builder.show()
    }

    private fun createDeleteConfirmation(id: Int, name: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.are_you_sure))
        builder.setMessage("${getString(R.string.delete_cap)} $name")
        builder.setPositiveButton(getString(R.string.delete_cap)) { _, _ ->
            deleteTopicRequest(id)
        }
        builder.setNeutralButton(getString(R.string.cancel_cap)) { _, _ -> } // do nothing
        builder.show()
    }

    override fun subscribeClicked() {
        // unreachable
    }

    override fun navigateToAll() {
        // do nothing
    }

    override fun navigateToSubscriptions() {
        compositeDisposable.dispose()
        findNavController().navigate(R.id.action_topicListFragment_to_subscriptionsFragment)
    }

    override fun navigateBack() {
        // do nothing
    }
}
