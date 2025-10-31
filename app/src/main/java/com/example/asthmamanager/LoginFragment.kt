package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.asthmamanager.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle the Login button click
        binding.buttonLogin.setOnClickListener {
            val selectedRole = binding.spinnerRole.selectedItem.toString()

            when (selectedRole) {
                "Select Role" -> {
                    Toast.makeText(requireContext(), "Please select a role", Toast.LENGTH_SHORT).show()
                }
                "Doctor" -> {
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToDoctorDashboardFragment())
                }
                else -> {
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeDashboardFragment())
                }
            }
        }

        // Handle the "Sign Up" link click
        binding.textSignupLink.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignupFragment())
        }

        // Handle the "Forgot Password" link click
        binding.textForgotPassword.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
