package com.example.lecturerecorder.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.lecturerecorder.R
import com.example.lecturerecorder.contract.NavigationContract
import com.example.lecturerecorder.recorder_activity.RecorderActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(), NavigationContract.Container {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_nav_search -> {
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    val fragment = navHostFragment!!.childFragmentManager.fragments[0]
                    (fragment as NavigationContract.Fragment).navigateToAll()
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
        }

        setSupportActionBar(findViewById(R.id.my_toolbar))
    }

    override fun setActionBarText(text: String) {
        supportActionBar?.title = text
    }

    override fun goToPreviewView(lectureId: Int) {
        Toast.makeText(this, "Open Preview View Here", Toast.LENGTH_SHORT).show()

    }

    override fun goToRecorderView(courseId: Int) {
        startActivity(Intent(this, RecorderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
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


}
