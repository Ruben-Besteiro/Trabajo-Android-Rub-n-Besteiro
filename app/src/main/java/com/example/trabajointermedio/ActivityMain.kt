package com.example.trabajointermedio

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.trabajointermedio.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var showOnlyFavorites = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initGUI()
    }

    private fun initGUI() {
        binding.filterFavoritesButton.setOnClickListener {
            showOnlyFavorites = !showOnlyFavorites
            updateFilterButtonText()
            showCurrentFragment()
        }
        updateFilterButtonText()
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainerCars) == null) {
            showCurrentFragment()
        }
    }

    private fun updateFilterButtonText() {
        binding.filterFavoritesButton.text = if (showOnlyFavorites) {
            "Ver todos"
        } else {
            "Ver solo favoritos"
        }
    }

    private fun showCurrentFragment() {
        val fragment: Fragment = if (showOnlyFavorites) {
            FavoriteCarsFragment()
        } else {
            AllCarsFragment()
        }
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerCars, fragment)
        }
    }
}