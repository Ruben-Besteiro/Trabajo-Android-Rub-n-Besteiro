package com.example.trabajointermedio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.trabajointermedio.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlin.io.root

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var name: String
    private lateinit var pass: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        auth = FirebaseAuth.getInstance()
        // Esta URL es la de arriba del todo
        database = FirebaseDatabase.getInstance("https://android-untar-la-manteca-default-rtdb.europe-west1.firebasedatabase.app/")
        name = requireArguments().getString("name").toString()
        pass = requireArguments().getString("pass").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            // Navigate to RegisterFragment using Navigation Component
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        // todas las acciones
        binding.btnRegistro.setOnClickListener {
            val email = binding.editCorreo.text.toString()
            val pass = binding.editPass.text.toString()
            val name = binding.editNombre.text.toString()
            val surname = binding.editApellido.text.toString()
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Podríamos volver al login o ir directamente a la main
                        Snackbar.make(binding.root, "Registro completado con éxito", Snackbar.LENGTH_LONG).show()
                        /*.setAction("OK") {
                            findNavController().navigate(R.id.action)
                        }*/

                        // Cuando creamos el usuario lo mete en la base de datos
                        database.reference.child("users")
                            .child(auth.currentUser!!.uid)
                            .child("name")
                            .setValue(name)

                        parentFragmentManager.popBackStack()
                    } else {
                        Snackbar.make(binding.root, "Registro erróneo: ${it.exception?.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
        binding.editCorreo.setText(name)
        binding.editPass.setText(pass)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}