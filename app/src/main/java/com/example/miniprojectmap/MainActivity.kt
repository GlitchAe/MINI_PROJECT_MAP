package com.example.miniprojectmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
// Hapus import untuk setupWithNavController jika tidak terpakai
// import androidx.navigation.ui.setupWithNavController
import com.example.miniprojectmap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.homeFragment3)
                    true
                }
                R.id.nav_calendar -> {
                    navController.navigate(R.id.calendarFragment2)
                    true
                }
                else -> false
            }
        }
    }
}