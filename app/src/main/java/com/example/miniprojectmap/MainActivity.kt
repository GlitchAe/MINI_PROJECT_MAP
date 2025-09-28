package com.example.miniprojectmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.miniprojectmap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Listener untuk ikon hamburger di toolbar agar membuka drawer
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Listener manual untuk item di dalam Navigation Drawer
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> navController.navigate(R.id.homeFragment3)
                R.id.nav_calendar -> navController.navigate(R.id.calendarFragment2)
                R.id.nav_birthday_list -> navController.navigate(R.id.birthdayListFragment)
                R.id.nav_profile -> navController.navigate(R.id.profileFragment)
            }
            // Tutup drawer setelah item diklik
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Listener manual untuk item di Bottom Navigation
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