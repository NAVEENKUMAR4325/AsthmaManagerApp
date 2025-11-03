package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.asthmamanager.databinding.FragmentSignupBinding
import com.example.asthmamanager.network.RetrofitClient
import com.example.asthmamanager.network.SignupRequest
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignup.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (role == "Select Role") {
                Toast.makeText(requireContext(), "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val signupRequest = SignupRequest(email = email, password = password, role = role)

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.signup(signupRequest)

                    if (response.isSuccessful) {
                        findNavController().navigate(SignupFragmentDirections.actionSignupFragmentToPatientDetailsFragment())
                    } else {
                        Toast.makeText(requireContext(), "Signup failed. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.textLoginLink.setOnClickListener {
            findNavController().navigate(SignupFragmentDirections.actionSignupFragmentToLoginFragment())
        }

        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
