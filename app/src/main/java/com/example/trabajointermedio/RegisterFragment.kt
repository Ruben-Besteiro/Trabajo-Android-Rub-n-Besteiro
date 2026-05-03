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
import com.google.firebase.database.FirebaseDatabase

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://android-untar-la-manteca-default-rtdb.europe-west1.firebasedatabase.app/")

        // Restore values from Bundle arguments
        val emailArg = arguments?.getString("email") ?: ""
        val passwordArg = arguments?.getString("password") ?: ""
        
        if (emailArg.isNotEmpty()) binding.emailEditText.setText(emailArg)
        if (passwordArg.isNotEmpty()) binding.passwordEditText.setText(passwordArg)

        binding.backButton.setOnClickListener {
            // Pass current data back to LoginFragment
            val bundle = Bundle().apply {
                putString("email", binding.emailEditText.text.toString())
                putString("password", binding.passwordEditText.text.toString())
            }
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment, bundle)
        }

        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.root, "Por favor, rellena todos los campos", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Añadimos al usuario a la tabla de usuarios
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid == null) {
                            Snackbar.make(binding.root, "No se pudo obtener el usuario recién creado", Snackbar.LENGTH_LONG).show()
                            return@addOnCompleteListener
                        }

                        val userData = mapOf(
                            "name" to name,
                            // Firebase RTDB elimina nodos null; con este marcador se garantiza que exista la sección.
                            "favs" to mapOf("_placeholder" to true)
                        )

                        database.reference.child("users")
                            .child(uid)
                            .setValue(userData)

                        Snackbar.make(binding.root, "Registro completado con éxito", Snackbar.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    } else {
                        Snackbar.make(binding.root, "Error: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}