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

        // Recuperamos los datos que nos hayan pasado desde el registro (si existen)
        val emailArg = arguments?.getString("email") ?: ""
        val passwordArg = arguments?.getString("password") ?: ""
        
        if (emailArg.isNotEmpty()) binding.emailEditText.setText(emailArg)
        if (passwordArg.isNotEmpty()) binding.passwordEditText.setText(passwordArg)

        // Acción al pulsar el botón de Iniciar Sesión
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.getText().toString()
            val password = binding.passwordEditText.getText().toString()

            // Si las cosas están vacías no nos deja entrar
            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.root, "Por favor, rellena todos los campos", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Nos logeamos con Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Snackbar.make(binding.root, "¡Bienvenido!", Snackbar.LENGTH_SHORT).show()
                        
                        // Si el login es correcto, vamos a la Activity principal
                        val intent = Intent(requireContext(), ActivityMain::class.java)
                        startActivity(intent)
                        activity?.finish()
                    } else {
                        Snackbar.make(binding.root, "Error: El usuario no existe o los datos son incorrectos", Snackbar.LENGTH_SHORT).show()
                    }
            }
        }

        // Acción para ir a la pantalla de Registro
        binding.registerButton.setOnClickListener {
            // Metemos datos para luego sacarlos en el otro fragmento
            val bundle = Bundle().apply {
                putString("email", binding.emailEditText.text.toString())
                putString("password", binding.passwordEditText.text.toString())
            }
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment, bundle)
        }

        // Botón para salir (cerrar la aplicación)
        binding.salirButton.setOnClickListener {
            activity?.finishAffinity()
        }
    }
}
