package com.example.trabajointermedio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trabajointermedio.R
import com.example.trabajointermedio.databinding.CarCardBinding
import com.example.trabajointermedio.model.Car

class CarAdapter(private var cars: Array<Car>, private val onFavClick: (Car) -> Unit) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {
    inner class CarViewHolder(val binding: CarCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = CarCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        holder.binding.apply {
            carName.text = car.name
            
            // Cargamos la imagen con Glide
            Glide.with(root.context)
                .load(car.image)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageProduct)

            buttonFav.text = if (car.favorite) "💙" else "🤍"
            buttonFav.setOnClickListener {
                onFavClick(car)
            }

            buttonDetalles.text = "Ver detalles"
            buttonDetalles.setOnClickListener {
                val yearText = car.year ?: "No disponible"
                // Al darle al botón mostramos un modal con información
                AlertDialog.Builder(root.context)
                    .setTitle(car.name)
                    .setMessage("Año de fabricación: $yearText")
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = cars.size

    // Al cambiar los datos actualizamos
    fun submitList(newCars: List<Car>) {
        this.cars = newCars.toTypedArray()
        notifyDataSetChanged()
    }
}
