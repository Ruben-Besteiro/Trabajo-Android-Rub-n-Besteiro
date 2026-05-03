package com.example.trabajointermedio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.trabajointermedio.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Restore values from Bundle arguments
        val emailArg = arguments?.getString("email") ?: ""
        val passwordArg = arguments?.getString("password") ?: ""
        
        if (emailArg.isNotEmpty()) binding.emailEditText.setText(emailArg)
        if (passwordArg.isNotEmpty()) binding.passwordEditText.setText(passwordArg)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.root, "Por favor, rellena todos los campos", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(binding.root, "Bienvenido", Snackbar.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), ActivityMain::class.java)
                        startActivity(intent)
                    } else {
                        Snackbar.make(binding.root, "El usuario no existe", Snackbar.LENGTH_SHORT).show()
                    }
            }
        }

        binding.registerButton.setOnClickListener {
            // Pass current data to RegisterFragment
            val bundle = Bundle().apply {
                putString("email", binding.emailEditText.text.toString())
                putString("password", binding.passwordEditText.text.toString())
            }
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment, bundle)
        }

        // Para salir, crasheamos la aplicación a propósito
        binding.salirButton.setOnClickListener {
            val fhqwhgads = null
            startActivity(fhqwhgads!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
