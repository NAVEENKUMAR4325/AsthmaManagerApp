package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.asthmamanager.databinding.FragmentPatientDetailsBinding
import com.example.asthmamanager.network.BaselinePEFRCreate
import com.example.asthmamanager.network.RetrofitClient
import com.example.asthmamanager.network.SignupRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PatientDetailsFragment : Fragment() {

    private var _binding: FragmentPatientDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonNext.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val age = binding.editTextAge.text.toString().toIntOrNull()
            val height = binding.editTextHeight.text.toString().toIntOrNull()
            val gender = binding.editTextGender.text.toString().trim()
            val contact = binding.editTextContact.text.toString().trim()
            val address = binding.editTextAddress.text.toString().trim()
            val baselinePefr = binding.editTextBaselinePefr.text.toString().toIntOrNull()

            if (name.isEmpty() || age == null || height == null || gender.isEmpty() || contact.isEmpty() || address.isEmpty() || baselinePefr == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val signupRequest = SignupRequest(
                email = "", // This will be updated later
                password = "", // This will be updated later
                role = "Patient",
                fullName = name,
                age = age,
                height = height,
                gender = gender,
                contactInfo = contact,
                address = address,
                baselinePefr = baselinePefr
            )

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.signup(signupRequest)
                    }

                    if (response.isSuccessful) {
                        val baselineRequest = BaselinePEFRCreate(baselinePefr)
                        val baselineResponse = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.setBaseline(baselineRequest)
                        }

                        if (baselineResponse.isSuccessful) {
                            findNavController().navigate(PatientDetailsFragmentDirections.actionPatientDetailsFragmentToHomeDashboardFragment())
                        } else {
                            Toast.makeText(requireContext(), "Failed to set baseline. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Signup failed. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
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
