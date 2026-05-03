package com.example.trabajointermedio

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trabajointermedio.adapter.CarAdapter
import com.example.trabajointermedio.databinding.ActivityMainBinding
import com.example.trabajointermedio.model.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var carAdapter: CarAdapter
    private val cars = mutableListOf<Car>()
    private val allCars = mutableListOf<Car>()
    private val favoriteIds = mutableSetOf<String>()
    private lateinit var database: FirebaseDatabase
    private var showOnlyFavorites = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        instances()
        initGUI()
        loadCarsIntoRecycler()
    }

    private fun instances() {
        database = FirebaseDatabase.getInstance("https://android-untar-la-manteca-default-rtdb.europe-west1.firebasedatabase.app/")
        carAdapter = CarAdapter(cars) { car ->
            addToFavorites(car)
        }
    }

    private fun initGUI() {
        binding.recyclerProductos.isNestedScrollingEnabled = true
        binding.filterFavoritesButton.setOnClickListener {
            showOnlyFavorites = !showOnlyFavorites
            updateFilterButtonText()
            renderCars()
        }
        updateFilterButtonText()

        // Si está en vertical o en horizontal es diferente
        if (resources.configuration.orientation == 1) {
            binding.recyclerProductos.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        } else {
            binding.recyclerProductos.layoutManager =
                GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false)
        }
        binding.recyclerProductos.adapter = carAdapter
    }

    private fun loadCarsIntoRecycler() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            database.reference.child("favs").child(uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        favoriteIds.clear()
                        favoriteIds.addAll(snapshot.children.mapNotNull { it.key })
                        refreshCarsFavoriteState()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ActivityMain", "Error al obtener favs", error.toException())
                    }
                })
        }

        database.reference.child("cars")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val mappedCars = snapshot.children.map { parseCar(it) }
                    allCars.clear()
                    allCars.addAll(mappedCars)
                    renderCars()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ActivityMain", "Error al obtener coches", error.toException())
                }
            })
    }

    private fun addToFavorites(car: Car) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Debes iniciar sesión para guardar favoritos", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (car.id.isBlank()) {
            Toast.makeText(this, "No se pudo identificar el coche", Toast.LENGTH_SHORT).show()
            return
        }

        database.reference.child("favs")
            .child(uid)
            .child(car.id)
            .setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar favorito", Toast.LENGTH_SHORT).show()
            }
    }

    private fun refreshCarsFavoriteState() {
        val updatedCars = allCars.map { car ->
            car.copy(favorite = favoriteIds.contains(car.id))
        }
        allCars.clear()
        allCars.addAll(updatedCars)
        renderCars()
    }

    private fun renderCars() {
        val carsToShow = if (showOnlyFavorites) {
            allCars.filter { it.favorite }
        } else {
            allCars
        }
        carAdapter.submitList(carsToShow)
    }

    private fun updateFilterButtonText() {
        binding.filterFavoritesButton.text = if (showOnlyFavorites) {
            "Ver todos"
        } else {
            "Ver solo favoritos"
        }
    }

    private fun parseCar(snapshot: DataSnapshot): Car {
        val value = snapshot.value
        if (value is String) {
            return Car(
                id = snapshot.key ?: "",
                name = value,
                favorite = favoriteIds.contains(snapshot.key ?: "")
            )
        }

        if (value is Map<*, *>) {
            val name = value["name"] as? String ?: "Coche sin nombre"
            val year = value["year"] as? Number
            val image = value["image"] as? String ?: ""
            val favorite = value["favorite"] as? Boolean ?: false
            val carId = snapshot.key ?: ""
            return Car(
                id = carId,
                name = name,
                year = year,
                image = image,
                favorite = favoriteIds.contains(carId) || favorite
            )
        }

        val carId = snapshot.key ?: ""
        return Car(
            id = carId,
            name = snapshot.key ?: "Coche",
            favorite = favoriteIds.contains(carId)
        )
    }
}