package com.example.lecturerecorder.view.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.lecturerecorder.R
import com.example.lecturerecorder.contract.NavigationContract
import com.example.lecturerecorder.model.LectureResponse
import com.example.lecturerecorder.model.ListElement
import com.example.lecturerecorder.model.ListElementType
import com.example.lecturerecorder.utils.RestClient
import com.example.lecturerecorder.utils.SpacedDividerItemDecoration
import com.example.lecturerecorder.utils.parseHttpErrorMessage
import com.example.lecturerecorder.view.adapters.ListAdapter
import com.example.lecturerecorder.viewmodel.ListViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recycler_list.*
import kotlinx.android.synthetic.main.fragment_recycler_list.view.*

class LectureListFragment : Fragment(), ListAdapter.OnSelectListener, NavigationContract.Fragment {


    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var compositeDisposable: CompositeDisposable
    private val model: ListViewModel by activityViewModels()
    private lateinit var broadcastReceiver: BroadcastReceiver

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
        setActionBarTitle(getString(R.string.lectures_cap))
        (activity as NavigationContract.Container).enableBackButton(true)

        // setup bottom floating button
        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener {
            (activity as NavigationContract.Container).goToRecorderView(model.selectedCourseId.value!!)
        }

        // setup header under appbar
        (activity as NavigationContract.Container).setHeaderVisibility(true)
        (activity as NavigationContract.Container).setSubscribeButtonVisibility(true)
        (activity as NavigationContract.Container).setHeaderTitle(
            model.selectedCourseName.value ?: ""
        )

        // setup swipe refresh
        swiperefresh.setOnRefreshListener {
            loadAndSetData()
        }

        // disable floating button for non-owned course
        if (model.isCourseOwned.value != null && !model.isCourseOwned.value!!) {
            fab_add.visibility = View.INVISIBLE
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

    override fun onResume() {
        super.onResume()

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                loadAndSetData()
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver,
            IntentFilter("com.example.lecturerecorder.AudioUploadService")
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    fun loadAndSetData() {
        requireView().swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.getLectures(
                model.selectedCourseId.value ?: return@loadAndSetData
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError)
        )
    }

    private fun handleResponse(lectures: List<LectureResponse>?) {
        val mappedList =
            lectures?.map {
                ListElement(
                    ListElementType.Short, if (it.isOwner) {
                        "${it.name} ${getString(R.string.owned_addition_text)}"
                    } else {
                        it.name
                    }, null, null, it.id, true
                )
            }

        if (mappedList.isNullOrEmpty()) {
            // set empty icon
            viewAdapter = ListAdapter(emptyList(), this)
            recyclerView.adapter = viewAdapter
            model.lectures.postValue(emptyList())
            model.lectureModels.postValue(emptyList())
            showEmptyListIndicator(true)
            setEmptyListText(getString(R.string.no_lectures_available_here))
        } else {
            viewAdapter = ListAdapter(mappedList, this)
            recyclerView.adapter = viewAdapter
            model.lectures.postValue(mappedList)
            model.lectureModels.postValue(lectures)
            showEmptyListIndicator(false)
        }
        swiperefresh?.isRefreshing = false
    }

    private fun handleError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        swiperefresh?.isRefreshing = false
    }

    // DELETE TOPIC ###########################################################################

    private fun deleteLectureRequest(lectureId: Int) {
        swiperefresh.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.deleteLecture(lectureId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::lectureDeleted, this::lectureDeleteError)
        )
    }

    private fun lectureDeleted() {
        Snackbar.make(requireView(), getString(R.string.lecture_deleted), Snackbar.LENGTH_SHORT)
            .show()
        loadAndSetData()
    }


    private fun lectureDeleteError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
        swiperefresh?.isRefreshing = false
    }

    // SUBSCRIBE TO COURSE ###########################################################################

    private fun subscribeCourseRequest(courseId: Int) {
        swiperefresh?.isRefreshing = true
        compositeDisposable.add(
            RestClient.listService.subscribeCourse(courseId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::courseSubscribed, this::courseSubscribeError)
        )
    }

    private fun courseSubscribed() {
        Snackbar.make(requireView(), getString(R.string.course_subscribed), Snackbar.LENGTH_SHORT)
            .show()
        swiperefresh?.isRefreshing = false
    }


    private fun courseSubscribeError(error: Throwable) {
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

    private fun setActionBarTitle(text: String) {
        (activity as AppCompatActivity).supportActionBar?.title = text;
    }

    companion object {
        @JvmStatic
        fun newInstance() = LectureListFragment()
    }

    override fun onSelect(position: Int) {
        try {
            val elem = model.lectures.value?.get(position) ?: return@onSelect
            model.lectureModels.value.let {
                var lecture: LectureResponse? = null
                for (l in model.lectureModels.value!!) {
                    if (l.id == elem.id) {
                        lecture = l
                    }
                }
                lecture?.let {
                    (activity as NavigationContract.Container).goToPreviewView(elem.id, lecture)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onLongSelect(position: Int) {
        val elem = model.lectures.value?.get(position) ?: return@onLongSelect
        if (model.isCourseOwned.value != null && model.isCourseOwned.value!!) {
            createDeleteConfirmation(elem.id, elem.title)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.this_is_not_your_lecture),
                Toast.LENGTH_LONG
            ).show()
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
        requireView().findViewById<RelativeLayout>(R.id.empty_list_info).visibility =
            getVisibGone(!state)
    }

    private fun setEmptyListText(text: String) {
        requireView().findViewById<TextView>(R.id.empty_list_text).text = text
    }

    private fun createDeleteConfirmation(lectureId: Int, name: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.are_you_sure))
        builder.setMessage("${getString(R.string.delete_cap)} $name")
        builder.setPositiveButton(getString(R.string.delete_cap)) { _, _ ->
            deleteLectureRequest(lectureId)
        }
        builder.setNeutralButton(getString(R.string.cancel_cap)) { _, _ -> } // do nothing
        builder.show()
    }

    override fun subscribeClicked() {
        subscribeCourseRequest(model.selectedCourseId.value ?: return)
    }

    override fun navigateToAll() {
        // do nothing
    }

    override fun navigateToSubscriptions() {
        compositeDisposable.dispose()
        findNavController().navigate(R.id.action_lectureListFragment_to_subscriptionsFragment)
    }

    override fun navigateBack() {
        compositeDisposable.dispose()
        findNavController().navigate(R.id.action_lectureListFragment_to_courseListFragment)
    }
}
