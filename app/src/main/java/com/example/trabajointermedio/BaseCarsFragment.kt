package com.example.trabajointermedio

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.trabajointermedio.adapter.CarAdapter
import com.example.trabajointermedio.databinding.FragmentCarsListBinding
import com.example.trabajointermedio.model.Car
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

abstract class BaseCarsFragment : Fragment(R.layout.fragment_cars_list) {
    private var _binding: FragmentCarsListBinding? = null
    protected val binding: FragmentCarsListBinding
        get() = _binding!!

    private lateinit var carAdapter: CarAdapter
    private val cars = mutableListOf<Car>()
    private val allCars = mutableListOf<Car>()
    private val favoriteIds = mutableSetOf<String>()
    private lateinit var requestQueue: RequestQueue
    private val baseUrl =
        "https://android-untar-la-manteca-default-rtdb.europe-west1.firebasedatabase.app"
    private val requestTag = "CarsFragmentRequests_${hashCode()}"

    protected abstract val onlyFavorites: Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCarsListBinding.bind(view)
        requestQueue = Volley.newRequestQueue(requireContext())
        carAdapter = CarAdapter(cars) { car ->
            addToFavorites(car)
        }
        initRecycler()
        loadCarsIntoRecycler()
    }

    private fun initRecycler() {
        binding.recyclerProductos.isNestedScrollingEnabled = true
        binding.recyclerProductos.layoutManager =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            } else {
                GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
            }
        binding.recyclerProductos.adapter = carAdapter
    }

    private fun loadCarsIntoRecycler() {
        fetchCars()
        fetchFavorites()
    }

    private fun fetchFavorites() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            favoriteIds.clear()
            refreshCarsFavoriteState()
            return
        }

        val url = "$baseUrl/favs/$uid.json"
        val request = StringRequest(
            Request.Method.GET,
            url,
            { responseText ->
                favoriteIds.clear()
                if (responseText != "null") {
                    try {
                        val json = JSONTokener(responseText).nextValue()
                        if (json is JSONObject) {
                            json.keys().forEach { key ->
                                if (json.optBoolean(key, false)) {
                                    favoriteIds.add(key)
                                }
                            }
                        } else if (json is JSONArray) {
                            for (i in 0 until json.length()) {
                                if (json.optBoolean(i, false)) {
                                    favoriteIds.add(i.toString())
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BaseCarsFragment", "Error parsing favorites", e)
                    }
                }
                refreshCarsFavoriteState()
            },
            { error ->
                favoriteIds.clear()
                refreshCarsFavoriteState()
                Log.e("BaseCarsFragment", "Error al obtener favs", error)
            }
        )
        request.tag = requestTag
        requestQueue.add(request)
    }

    private fun fetchCars() {
        val url = "$baseUrl/cars.json"
        val request = StringRequest(
            Request.Method.GET,
            url,
            { responseText ->
                if (responseText == "null") {
                    allCars.clear()
                    renderCars()
                    return@StringRequest
                }

                val mappedCars = mutableListOf<Car>()
                try {
                    val json = JSONTokener(responseText).nextValue()
                    if (json is JSONObject) {
                        json.keys().forEach { carId ->
                            val carValue = json.opt(carId)
                            mappedCars.add(parseCar(carId, carValue))
                        }
                    } else if (json is JSONArray) {
                        for (i in 0 until json.length()) {
                            if (!json.isNull(i)) {
                                mappedCars.add(parseCar(i.toString(), json.opt(i)))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BaseCarsFragment", "Error parsing cars", e)
                }

                allCars.clear()
                allCars.addAll(mappedCars)
                refreshCarsFavoriteState()
            },
            { error ->
                Log.e("BaseCarsFragment", "Error al obtener coches", error)
                Toast.makeText(requireContext(), "Error al cargar coches", Toast.LENGTH_SHORT).show()
            }
        )
        request.tag = requestTag
        requestQueue.add(request)
    }

    private fun addToFavorites(car: Car) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(
                requireContext(),
                "Debes iniciar sesión para guardar favoritos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (car.id.isBlank()) {
            Toast.makeText(requireContext(), "No se pudo identificar el coche", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val isFavorite = favoriteIds.contains(car.id)
        val url = "$baseUrl/favs/$uid/${car.id}.json"
        val request = if (isFavorite) {
            StringRequest(
                Request.Method.DELETE,
                url,
                {
                    favoriteIds.remove(car.id)
                    refreshCarsFavoriteState()
                    Toast.makeText(requireContext(), "Quitado de favoritos", Toast.LENGTH_SHORT)
                        .show()
                },
                { error ->
                    Log.e("BaseCarsFragment", "Error al quitar favorito", error)
                    Toast.makeText(
                        requireContext(),
                        "Error al quitar favorito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            object : StringRequest(
                Request.Method.PUT,
                url,
                {
                    favoriteIds.add(car.id)
                    refreshCarsFavoriteState()
                    Toast.makeText(requireContext(), "Añadido a favoritos", Toast.LENGTH_SHORT)
                        .show()
                },
                { error ->
                    Log.e("BaseCarsFragment", "Error al guardar favorito", error)
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar favorito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {
                override fun getBodyContentType(): String = "application/json; charset=utf-8"

                override fun getBody(): ByteArray = "true".toByteArray(Charsets.UTF_8)
            }
        }
        request.tag = requestTag
        requestQueue.add(request)
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
        val carsToShow = if (onlyFavorites) {
            allCars.filter { it.favorite }
        } else {
            allCars
        }
        carAdapter.submitList(carsToShow)
    }

    private fun parseCar(carId: String, value: Any?): Car {
        if (value is String) {
            return Car(
                id = carId,
                name = value,
                favorite = favoriteIds.contains(carId)
            )
        }

        if (value is JSONObject) {
            val name = value.optString("name", "Coche sin nombre")
            val year = value.optInt("year", -1).takeIf { it >= 0 }
            val image = value.optString("image", "")
            val favorite = value.optBoolean("favorite", false)
            return Car(
                id = carId,
                name = name,
                year = year,
                image = image,
                favorite = favoriteIds.contains(carId) || favorite
            )
        }

        return Car(
            id = carId,
            name = carId.ifBlank { "Coche" },
            favorite = favoriteIds.contains(carId)
        )
    }

    override fun onDestroyView() {
        requestQueue.cancelAll(requestTag)
        _binding = null
        super.onDestroyView()
    }
}
