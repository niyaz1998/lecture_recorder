package com.example.lecturerecorder.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.lecturerecorder.R
import com.example.lecturerecorder.contract.NavigationContract
import com.example.lecturerecorder.model.LectureResponse
import com.example.lecturerecorder.player_activity.ListenerActivity
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
                    true
                }

                R.id.bottom_nav_my -> {
                    true
                }

                R.id.bottom_nav_settings -> {
                    true
                }

                else -> {
                    true
                }
            }
        }

        setSupportActionBar(findViewById(R.id.my_toolbar))
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.appbar_menu, menu)
//        return true;
//    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_favorite -> {
            Toast.makeText(this, "Like Button Selected", Toast.LENGTH_LONG).show()
            setActionBarText("New action bar text")
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
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


}
