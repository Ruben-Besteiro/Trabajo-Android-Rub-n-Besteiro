package com.example.trabajointermedio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trabajointermedio.R
import com.example.trabajointermedio.databinding.CarCardBinding
import com.example.trabajointermedio.model.Car

class CarAdapter(
    private var cars: List<Car>,
    private val onFavClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(val binding: CarCardBinding) : RecyclerView.ViewHolder(binding.root)

    // Aquí se infla el layout de la fila y lo devolvemos
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val b = CarCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(b)
    }

    // Aquí metemos las cosas
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        val b = holder.binding

        b.carName.text = car.name

        // Carga de imagen directa
        Glide.with(b.root.context).load(car.image).placeholder(R.mipmap.ic_launcher).into(b.imageProduct)

        // Botón de favorito
        b.buttonFav.text = if (car.favorite) "💙" else "🤍"
        b.buttonFav.setOnClickListener {
            onFavClick(car)     // Llama a handleFavoriteClick en el fragmento
        }

        // En el botón de detalles hau un escuchador que muestra un modal
        b.buttonDetalles.setOnClickListener {
            AlertDialog.Builder(b.root.context)
                .setTitle(car.name)
                .setMessage("Año: ${car.year ?: "Desconocido"}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun getItemCount() = cars.size

    // Método de actualización de la lista
    fun submitList(newList: List<Car>) {
        cars = newList
        notifyDataSetChanged()
    }
}