package com.example.lecturerecorder.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.lecturerecorder.R
import com.example.lecturerecorder.contract.NavigationContract
import com.example.lecturerecorder.login_activity.LoginActivity
import com.example.lecturerecorder.model.LectureResponse
import com.example.lecturerecorder.player_activity.ListenerActivity
import com.example.lecturerecorder.recorder_activity.RecorderActivity
import com.example.lecturerecorder.utils.storeAuthToken
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationContract.Container {

    private var skipAction = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        bottom_nav.setupWithNavController(navController)
        bottom_nav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_nav_search -> {
                    if (!skipAction) {
                        // `skipAction` is needed when other parts of code do custom navigation
                        // but setting bottom nav icon causes this navigation code to run as well
                        // which interferes custom behaviour
                        currentFragment()?.navigateToAll()
                    }
                    skipAction = false
                    true
                }

                R.id.bottom_nav_subscriptions -> {
                    currentFragment()?.navigateToSubscriptions()
                    true
                }

                else -> {
                    true
                }
            }
        }

        subscribe_button.setOnClickListener {
            currentFragment()?.subscribeClicked()
        }
        setSupportActionBar(my_toolbar)
    }

    // get currently displayed fragment, null if it does not implement contract interface
    private fun currentFragment(): NavigationContract.Fragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        return navHostFragment!!.childFragmentManager.fragments[0] as? NavigationContract.Fragment
    }

    // enable custom appbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        return true;
    }

    // handle appbar clicks
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            currentFragment()?.navigateBack()
            true
        }

        R.id.action_logout -> {
            storeAuthToken("")
            startActivity(Intent(this, LoginActivity::class.java))
            true
        }
        else -> true
    }

    // change title in appbar
    override fun setActionBarText(text: String) {
        supportActionBar?.title = text
    }

    // open activity to record new lecture
    override fun goToPreviewView(lectureId: Int, lecture: LectureResponse) {
        startActivity(Intent(this, ListenerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ListenerActivity.ARGUMENTS, lecture)
        })
    }

    // open activity to listen recording
    override fun goToRecorderView(courseId: Int) {
        startActivity(Intent(this, RecorderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("courseId", courseId)
        })
    }

    // log out and go to login screen
    fun goToLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
    }

    // show/hide back button in appbar
    override fun enableBackButton(state: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(state)
    }

    // set title of
    override fun setHeaderTitle(text: String) {
        header_title?.text = text
    }

    // show appended panel
    override fun setHeaderVisibility(state: Boolean) {
        if (state) {
            header_bar?.visibility = View.VISIBLE
        } else {
            header_bar?.visibility = View.GONE
        }
    }

    // show/hide subscribe button in header
    override fun setSubscribeButtonVisibility(state: Boolean) {
        if (state) {
            subscribe_button?.visibility = View.VISIBLE
        } else {
            subscribe_button?.visibility = View.GONE
        }
    }

    // set bottom navigation to first element
    override fun resetNavigation() {
        skipAction = true
        bottom_nav.selectedItemId = R.id.bottom_nav_search
    }
}
