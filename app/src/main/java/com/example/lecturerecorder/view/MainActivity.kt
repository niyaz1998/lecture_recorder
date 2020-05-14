package com.example.lecturerecorder.view

import android.content.Intent
import android.os.Bundle
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
import com.example.lecturerecorder.model.LectureResponse
import com.example.lecturerecorder.player_activity.ListenerActivity
import com.example.lecturerecorder.recorder_activity.RecorderActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationContract.Container {

    private var skipAction = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_nav_search -> {
                    if (!skipAction) {
                        skipAction = false
                        val navHostFragment =
                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
                        (fragment as NavigationContract.Fragment).navigateToAll()
                    }
                    true
                }

//                R.id.bottom_nav_my -> {
//                    val navHostFragment =
//                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
//                    val fragment = navHostFragment!!.childFragmentManager.fragments[0]
//                    (fragment as NavigationContract.Fragment).navigateToPersonal()
//                    true
//                }

                R.id.bottom_nav_settings -> {
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    val fragment = navHostFragment!!.childFragmentManager.fragments[0]
                    (fragment as NavigationContract.Fragment).navigateToSubscriptions()
                    true
                }

                else -> {
                    true
                }
            }
        }

        findViewById<Button>(R.id.subscribe_button).setOnClickListener {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            val fragment = navHostFragment!!.childFragmentManager.fragments[0]
            (fragment as NavigationContract.Fragment).subscribeClicked()

//        setSupportActionBar(findViewById(R.id.my_toolbar))
//    }
//
////    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
////        menuInflater.inflate(R.menu.appbar_menu, menu)
////        return true;
////    }
//
//    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//        R.id.action_favorite -> {
//            Toast.makeText(this, "Like Button Selected", Toast.LENGTH_LONG).show()
//            setActionBarText("New action bar text")
//            true

        }

        setSupportActionBar(findViewById(R.id.my_toolbar))
    }

    override fun setActionBarText(text: String) {
        supportActionBar?.title = text
    }

    override fun goToPreviewView(
        lectureId: Int,
        lecture: LectureResponse
    ) {
        // Toast.makeText(this, "Open Preview View Here", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ListenerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ListenerActivity.ARGUMENTS, lecture)
        })
    }

    override fun goToRecorderView(courseId: Int) {
        startActivity(Intent(this, RecorderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("courseId", courseId)
        })
    }

    override fun setHeaderTitle(text: String) {
        findViewById<TextView>(R.id.header_title).setText(text)
    }

    override fun setHeaderVisibility(state: Boolean) {
        val header = findViewById<ConstraintLayout>(R.id.header_bar)
        if (state) {
            header.visibility = View.VISIBLE
        } else {
            header.visibility = View.GONE
        }
    }

    override fun resetNavigation() {
        skipAction = true
        findViewById<BottomNavigationView>(R.id.bottom_nav).selectedItemId = R.id.bottom_nav_search
    }


}
