package com.example.trabajointermedio

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

// Clase base para las listas de coches
abstract class CarsFragment : Fragment(R.layout.fragment_cars_list) {
    private var _binding: FragmentCarsListBinding? = null
    protected val binding get() = _binding!!

    private lateinit var carAdapter: CarAdapter

    private var allCarsList = emptyArray<Car>()
    private var favoriteIds = emptySet<String>()
    private lateinit var requestQueue: RequestQueue
    
    private val BASE_URL = "https://android-untar-la-manteca-default-rtdb.europe-west1.firebasedatabase.app"
    protected abstract val onlyFavorites: Boolean       // Abstract porque solo se modifica en los fragmentos concretos

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCarsListBinding.bind(view)
        
        // Inicializamos la cola de peticiones de Volley
        requestQueue = Volley.newRequestQueue(requireContext())

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        // Inicializamos el adaptador con una lista vacía especificando el tipo
        carAdapter = CarAdapter(emptyList()) {
            handleFavoriteClick(it)
        }

        // Esta línea es la que enlaza el adaptador con la parte gráfica
        binding.recyclerCoches.adapter = carAdapter
        
        // Configuramos el layout que tendrá el recycler
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        binding.recyclerCoches.layoutManager = if (isPortrait) {
            LinearLayoutManager(requireContext())
        } else {
            GridLayoutManager(requireContext(), 2)
        }
    }

    private fun loadData() {
        downloadCars()
        downloadFavorites()
    }

    private fun downloadCars() {
        val request = StringRequest(Request.Method.GET, "$BASE_URL/cars.json", { response ->
            if (response == "null") return@StringRequest
            
            val tempCars = mutableListOf<Car>()
            try {
                val json = JSONTokener(response).nextValue()
                if (json is JSONObject) {
                    // Si viene como objeto JSON
                    json.keys().forEach { id -> 
                        tempCars.add(parseCarObject(id, json.get(id))) 
                    }
                } else if (json is JSONArray) {
                    // Si viene como array JSON
                    for (i in 0 until json.length()) {
                        if (!json.isNull(i)) {
                            tempCars.add(parseCarObject(i.toString(), json.get(i)))
                        }
                    }
                }
            } catch (e: Exception) { 
                Log.e("API_ERROR", "Error al leer los coches", e) 
            }
            
            // Reemplazamos el array completo
            allCarsList = tempCars.toTypedArray()
            refreshDisplay()
        }, {
            Snackbar.make(binding.root, "Error de red al cargar coches", Snackbar.LENGTH_SHORT).show()
        })
        requestQueue.add(request)
    }

    private fun downloadFavorites() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        val request = StringRequest(Request.Method.GET, "$BASE_URL/favs/$uid.json", { response ->
            if (response != "null") {
                val tempFavs = mutableSetOf<String>()
                try {
                    val json = JSONTokener(response).nextValue()
                    if (json is JSONObject) {
                        json.keys().forEach { if (json.optBoolean(it)) tempFavs.add(it) }
                    } else if (json is JSONArray) {
                        for (i in 0 until json.length()) {
                            if (json.optBoolean(i)) tempFavs.add(i.toString())
                        }
                    }
                } catch (e: Exception) { 
                    Log.e("API_ERROR", "Error al leer los favoritos", e) 
                }
                // Asignamos el nuevo conjunto de favoritos
                favoriteIds = tempFavs.toSet()
            } else {
                favoriteIds = emptySet()
            }
            refreshDisplay()
        }, {})
        requestQueue.add(request)
    }

    private fun handleFavoriteClick(car: Car) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val isAlreadyFav = favoriteIds.contains(car.id)
        
        // Si ya es favorito lo borramos, pero si no lo metemos
        val method = if (isAlreadyFav) Request.Method.DELETE else Request.Method.PUT
        val url = "$BASE_URL/favs/$uid/${car.id}.json"
        
        val request = object : StringRequest(method, url, {
            // Actualizamos el conjunto creando uno nuevo con el cambio
            favoriteIds = if (isAlreadyFav) {
                favoriteIds - car.id
            } else {
                favoriteIds + car.id
            }

            refreshDisplay()
            
            val msg = if (isAlreadyFav) "Eliminado de favoritos" else "¡Añadido a favoritos!"
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
        }, {
            Snackbar.make(binding.root, "Error al actualizar favoritos", Snackbar.LENGTH_SHORT).show()
        }) {
            override fun getBody(): ByteArray = "true".toByteArray()
        }
        requestQueue.add(request)
    }

    private fun refreshDisplay() {
        // Actualizamos el estado de favorito y filtramos si estamos en la pestaña de favoritos
        val updatedList = allCarsList.map { 
            it.copy(favorite = favoriteIds.contains(it.id)) 
        }
        
        val finalList = if (onlyFavorites) {
            updatedList.filter { it.favorite }
        } else {
            updatedList
        }

        // El adapter actualiza la lista cada vez que le damos al corazón y al abrir la aplicación
        carAdapter.submitList(finalList)
    }

    private fun parseCarObject(id: String, data: Any?): Car {
        return if (data is JSONObject) {
            Car(
                id = id,
                name = data.optString("name", "Coche desconocido"),
                year = data.optInt("year", -1).takeIf { it >= 0 },
                image = data.optString("image", ""),
                favorite = favoriteIds.contains(id)
            )
        } else {
            // Caso simple si el valor es solo el nombre
            Car(id = id, name = data.toString(), favorite = favoriteIds.contains(id))
        }
    }

    override fun onDestroyView() {
        // Hacemos salida ordenada
        requestQueue.cancelAll { true }
        _binding = null
        super.onDestroyView()
    }
}
