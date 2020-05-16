package com.example.lecturerecorder.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
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
import com.example.lecturerecorder.view.MainActivity
import com.example.lecturerecorder.viewmodel.ListViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recycler_list.*

class SubscriptionsFragment : Fragment(), ListAdapter.OnSelectListener,
    NavigationContract.Fragment {


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

        // setup recyclerview
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
        setActionBarTitle(getString(R.string.subscribed_courses))

        fab_add.visibility = getVisibGone(true)
//        fab.setOnClickListener {
//            (activity as NavigationContract.Container).goToRecorderView(model.selectedCourseId.value!!)
//        }

        // setup header under appbar
        (activity as NavigationContract.Container).setHeaderVisibility(false)
        (activity as NavigationContract.Container).enableBackButton(false)

        // setup swipe refresh
        swiperefresh.setOnRefreshListener {
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

        // load data
        loadAndSetData()
    }

    fun loadAndSetData() {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.getSubs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError)
        )
    }

    private fun handleResponse(response: SubResponse) {
        val mappedList = response.courses.map {
            ListElement(
                ListElementType.Detailed,
                it.name,
                it.description,
                "",
                it.id,
                it.isOwner
            )
        }

        if (mappedList.isNullOrEmpty()) {
            // set empty icon
            viewAdapter = ListAdapter(emptyList(), this)
            recyclerView.adapter = viewAdapter
            model.subscriptions.postValue(emptyList())
            showEmptyListIndicator(true)
            setEmptyListText(getString(R.string.no_lectures_available_here))
        } else {
            viewAdapter = ListAdapter(mappedList, this)
            recyclerView.adapter = viewAdapter
            model.subscriptions.postValue(mappedList)
            showEmptyListIndicator(false)
        }
        swiperefresh?.isRefreshing = false
    }

    private fun handleError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        swiperefresh?.isRefreshing = false
    }

    // UNSUB COURSE ###########################################################################

    private fun unsubCourseRequest(courseId: Int) {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.unsubCourse(courseId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::courseUnsub, this::courseUnsubError)
        )
    }

    private fun courseUnsub() {
        Snackbar.make(requireView(), getString(R.string.course_unsubscribed), Snackbar.LENGTH_SHORT)
            .show()
        loadAndSetData()
    }


    private fun courseUnsubError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
        swiperefresh?.isRefreshing = false
    }

    // UTILS ###########################################################################

    override fun onDestroy() {
        if (this::compositeDisposable.isInitialized) {
            compositeDisposable.clear()
        }
        super.onDestroy()
    }

    fun setActionBarTitle(text: String) {
        (activity as AppCompatActivity).supportActionBar?.title = text;
    }

    companion object {
        @JvmStatic
        fun newInstance() = LectureListFragment()
    }

    override fun onSelect(position: Int) {
        val elem = model.subscriptions.value?.get(position) ?: return@onSelect
        model.selectedCourseId.postValue(elem.id)
        model.selectedCourseName.postValue(elem.title)
        model.isCourseOwned.postValue(elem.isEditable)
        (activity as NavigationContract.Container).resetNavigation()
        view?.findNavController()
            ?.navigate(R.id.action_subscriptionsFragment_to_lectureListFragment)
    }

    override fun onLongSelect(position: Int) {
        val elem = model.subscriptions.value?.get(position) ?: return@onLongSelect
        createDeleteConfirmation(elem.id, elem.title)
    }

    private fun getVisibGone(s: Boolean): Int {
        return if (s) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showEmptyListIndicator(state: Boolean) {
        requireView().findViewById<RelativeLayout>(R.id.empty_list_info).visibility =
            getVisibGone(!state)
    }

    private fun setEmptyListText(text: String) {
        requireView().findViewById<TextView>(R.id.empty_list_text).text = text
    }

    private fun createDeleteConfirmation(courseId: Int, name: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.are_you_sure))
        builder.setMessage("${getString(R.string.unsubscribe_cap)} $name")
        builder.setPositiveButton(getString(R.string.unsubscribe_cap)) { _, _ ->
            unsubCourseRequest(courseId)
        }
        builder.setNeutralButton(getString(R.string.cancel_cap)) { _, _ -> } // do nothing
        builder.show()
    }

    override fun subscribeClicked() {
        // do nothing
    }

    override fun navigateToAll() {
        compositeDisposable.dispose()
        findNavController().navigate(R.id.action_subscriptionsFragment_to_topicListFragment)
    }

    override fun navigateToSubscriptions() {
        // do nothing
    }

    override fun navigateBack() {
        // do nothing
    }
}
