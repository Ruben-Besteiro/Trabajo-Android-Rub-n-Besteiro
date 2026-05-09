package com.example.trabajointermedio

// Este fragmento y el de ShowFavoriteCars heredan de CarsFragment. El susodicho no se utiliza
// Lo que hacemos al cambiar de fragmento es cambiar el booleano entre true y false
class ShowAllCarsFragment : CarsFragment() {
    override val onlyFavorites: Boolean = false
}
