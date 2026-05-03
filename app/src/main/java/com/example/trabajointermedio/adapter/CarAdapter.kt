package com.example.trabajointermedio.adapter

import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trabajointermedio.R
import com.example.trabajointermedio.model.Car

class CarAdapter(
    private val cars: MutableList<Car>,
    private val onFavClick: (Car) -> Unit
) :
    RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        val buttonFav: Button = itemView.findViewById(R.id.buttonFav)
        val buttonDetalles: Button = itemView.findViewById(R.id.buttonDetalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.car_card, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.productName.text = car.name
        Glide.with(holder.itemView.context)
            .load(car.image)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(holder.imageProduct)
        holder.buttonFav.text = if (car.favorite) "💙" else "🤍"
        holder.buttonFav.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onFavClick(cars[currentPosition])
            }
        }
        holder.buttonDetalles.text = "Ver detalles"
        holder.buttonDetalles.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            val currentCar = cars[currentPosition]
            val yearText = currentCar.year ?: "No disponible"
            // Al darle al botón, se abre un modal que muestra información
            AlertDialog.Builder(holder.itemView.context)
                .setTitle(currentCar.name)
                .setMessage("Año: $yearText")
                .setPositiveButton("Cerrar", null)
                .show()
        }
    }

    override fun getItemCount(): Int = cars.size

    fun submitList(newCars: List<Car>) {
        cars.clear()
        cars.addAll(newCars)
        notifyDataSetChanged()
    }
}