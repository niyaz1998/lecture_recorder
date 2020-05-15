package com.example.lecturerecorder.view.fragments

import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.example.lecturerecorder.R
import com.example.lecturerecorder.contract.NavigationContract
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
import kotlinx.android.synthetic.main.fragment_recycler_list.view.*

class CourseListFragment : Fragment(), ListAdapter.OnSelectListener, NavigationContract.Fragment {

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

        // setup recycler view
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

        // setup appbar
        setActionBarTitle(getString(R.string.courses))
        (activity as NavigationContract.Container).enableBackButton(true)

        // setup header under appbar
        (activity as NavigationContract.Container).setHeaderVisibility(true)
        (activity as NavigationContract.Container).setSubscribeButtonVisibility(false)
        (activity as NavigationContract.Container).setHeaderTitle(
            model.selectedTopicName.value ?: ""
        )

        // setup bottom floating button
        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener {
            createAdditionDialog()
        }

        // setup swipe refresh
        swiperefresh.setOnRefreshListener {
            loadAndSetData()
        }

        // disable floating button for non-owned course
        if (model.isTopicOwned.value != null && !model.isTopicOwned.value!!) {
            fab_add?.visibility = View.INVISIBLE
        }

        // override back button actions
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Snackbar.make(requireView(), getString(R.string.use_home_button_to_exit), Snackbar.LENGTH_SHORT)
                    .show()
                true
            } else {
                false
            }
        }

        loadAndSetData()
    }

    // LOAD LIST DATA ###########################################################################

    fun loadAndSetData() {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.getCourses(model.selectedTopicId.value ?: return@loadAndSetData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError)
        )
    }

    private fun handleResponse(courses: List<CourseResponse>?) {
        val mappedList = courses?.map {
            ListElement(
                ListElementType.Detailed,
                if (it.isOwner) {
                    "${it.name} ${getString(R.string.owned_addition_text)}"
                } else {
                    it.name
                },
                it.description, "${it.audios} ${getString(
                    R.string.lectures_underscore
                )}",
                it.id, it.isOwner
            )
        }

        if (mappedList.isNullOrEmpty()) {
            // set empty icon
            viewAdapter = ListAdapter(emptyList(), this)
            recyclerView.adapter = viewAdapter
            model.courses.postValue(emptyList())
            showEmptyListIndicator(true)
            setEmptyListText(getString(R.string.no_courses_available_here))
        } else {
            viewAdapter = ListAdapter(mappedList, this)
            recyclerView.adapter = viewAdapter
            model.courses.postValue(mappedList)
            showEmptyListIndicator(false)
        }
        swiperefresh?.isRefreshing = false
    }

    private fun handleError(error: Throwable) {
        //load data from repo
        val message = parseHttpErrorMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        swiperefresh?.isRefreshing = false
    }

    // CREATE COURSE ###########################################################################

    private fun createCourseRequest(topicId: Int, name: String, description: String) {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.createCourse(topicId, CoursePost(name, description))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::courseCreated, this::courseCreateError)
        )
    }

    private fun courseCreated(response: CourseResponse) {
        Snackbar.make(requireView(), getString(R.string.course_created), Snackbar.LENGTH_SHORT)
            .show()
        loadAndSetData()
    }

    private fun courseCreateError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_LONG
        ).show()
        swiperefresh?.isRefreshing = false
    }

    // PUT COURSE ###########################################################################

    private fun putCourseRequest(topicId: Int, courseId: Int, name: String, description: String) {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.putCourse(topicId, courseId, CoursePost(name, description))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::coursePut, this::coursePutError)
        )
    }

    private fun coursePut(response: CourseResponse) {
        Snackbar.make(requireView(), getString(R.string.course_updated), Snackbar.LENGTH_SHORT)
            .show()
        loadAndSetData()
    }

    private fun coursePutError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .show()
        swiperefresh?.isRefreshing = false
    }

    // DELETE COURSE ###########################################################################

    private fun deleteCourseRequest(topicid: Int, courseId: Int) {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.deleteCourse(topicid, courseId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::courseDeleted, this::courseDeleteError)
        )
    }

    private fun courseDeleted() {
        Snackbar.make(requireView(), getString(R.string.course_deleted), Snackbar.LENGTH_SHORT)
            .show()
        loadAndSetData()
    }

    private fun courseDeleteError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .show()
        swiperefresh?.isRefreshing = false
    }

    // UTILS ###########################################################################

    override fun onDestroy() {
        compositeDisposable.dispose()
        compositeDisposable.clear()
        super.onDestroy()
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
        val elem = model.courses.value?.get(position) ?: return@onSelect
        model.selectedCourseId.postValue(elem.id)
        model.selectedCourseName.postValue(elem.title)
        model.isCourseOwned.postValue(elem.isEditable)
        view?.findNavController()?.navigate(R.id.action_courseListFragment_to_lectureListFragment)
    }

    override fun onLongSelect(position: Int) {
        val elem = model.courses.value?.get(position) ?: return@onLongSelect
        if (model.isTopicOwned.value != null && model.isTopicOwned.value!!) {
            createEditDialog(
                model.selectedTopicId.value ?: 0,
                elem.id,
                elem.title,
                elem.description ?: ""
            )
        } else {
            Toast.makeText(requireContext(), getString(R.string.not_your_course), Toast.LENGTH_LONG).show()
        }
    }


    fun getVisibGone(s: Boolean): Int {
        return if (s) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showEmptyListIndicator(state: Boolean) {
        requireView().empty_list_info?.visibility =
            getVisibGone(!state)
    }

    private fun setEmptyListText(text: String) {
        requireView().empty_list_text?.text = text
    }

    private fun createAdditionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.new_course))
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

        builder.setNeutralButton(getString(R.string.cancel_cap)) { _, _ -> }
        builder.show()
    }

    private fun createEditDialog(topicId: Int, courseId: Int, name: String, description: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.edit_course))
        val innerView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.creation_dialog_layout, null)

        val nameField = innerView.findViewById<TextInputLayout>(R.id.name_field)
        val descrField = innerView.findViewById<TextInputLayout>(R.id.description_field)
        nameField.editText?.setText(name.dropLast(5)) // to remove ` (my)`
        descrField.editText?.setText(description)

        builder.setView(innerView)
        builder.setPositiveButton(getString(R.string.save_cap)) { _, _ ->
            val nt = nameField.editText?.text.toString().trim()
            val dt = descrField.editText?.text.toString().trim()
            putCourseRequest(topicId, courseId, nt, dt)
        }

        builder.setNegativeButton(getString(R.string.delete_cap)) { dialog, _ ->
            createDeleteConfirmation(topicId, courseId, name)
            dialog.dismiss()
        }

        builder.setNeutralButton(getString(R.string.cancel_cap)) { _, _ -> } // do nothing
        builder.show()
    }

    private fun createDeleteConfirmation(topicId: Int, courseId: Int, name: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.are_you_sure))
        builder.setMessage("${getString(R.string.delete_cap)} $name")
        builder.setPositiveButton(getString(R.string.delete_cap)) { _, _ ->
            deleteCourseRequest(topicId, courseId)
        }
        builder.setNeutralButton(getString(R.string.cancel_cap)) { _, _ -> } // do nothing
        builder.show()
    }

    override fun subscribeClicked() {
        // do nothing
    }

    override fun navigateToAll() {
        compositeDisposable.dispose()
        loadAndSetData()
    }

    override fun navigateToSubscriptions() {
        compositeDisposable.dispose()
        findNavController().navigate(R.id.action_courseListFragment_to_subscriptionsFragment)
    }

    override fun navigateBack() {
        compositeDisposable.dispose()
        findNavController().navigate(R.id.action_courseListFragment_to_topicListFragment)
    }

}
