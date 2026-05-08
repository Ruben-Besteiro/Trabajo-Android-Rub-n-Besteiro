package com.example.trabajointermedio

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.trabajointermedio.databinding.ActivityMainBinding

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var isShowingFavorites = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainerCars) as NavHostFragment
        navController = navHost.navController

        // Configuramos el grafo de navegación manualmente para que empiece en la lista de coches
        // De esta forma evitamos que vuelva a salir la pantalla de login al entrar aquí
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(R.id.allCarsFragment)
        navController.graph = navGraph

        // Al pulsar el botón de filtrar por favoritos
        binding.filterFavoritesButton.setOnClickListener {
            isShowingFavorites = !isShowingFavorites
            updateUI()
            navigateToList()
        }

        updateUI()
    }

    // Método para cambiar el texto del botón según lo que estemos viendo
    private fun updateUI() {
        binding.filterFavoritesButton.text =
            if (isShowingFavorites) "Ver todos"
            else "Ver favoritos"
    }

    // Método para cambiar de fragmento usando el grafo de navegación
    private fun navigateToList() {
        if (isShowingFavorites) {
            navController.navigate(R.id.action_allCarsFragment_to_showFavoriteCarsFragment)
        } else {
            navController.navigate(R.id.action_showFavoriteCarsFragment_to_allCarsFragment)
        }
    }
}
