package com.example.trabajointermedio

// Lo único que cambia entre fragmentos es un solo booleano, y se mira en el onViewCreated
// (después de pasar por funciones intermedias)
class ShowAllCarsFragment : CarsFragment() {
    override val onlyFavorites: Boolean = false
}
