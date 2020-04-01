package com.example.lecturerecorder.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.lecturerecorder.R
import com.example.lecturerecorder.contract.NavigationContract
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), NavigationContract.Container {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
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
            Toast.makeText(this,"Like Button Selected", Toast.LENGTH_LONG).show()
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
}
