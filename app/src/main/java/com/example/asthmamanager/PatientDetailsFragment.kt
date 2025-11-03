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
            val baselineValue = binding.editTextBaselinePefr.text.toString().toIntOrNull()

            if (baselineValue == null) {
                Toast.makeText(requireContext(), "Please enter a valid baseline PEFR value", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val baselineRequest = BaselinePEFRCreate(baselineValue)

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.setBaseline(baselineRequest)
                    }

                    if (response.isSuccessful) {
                        findNavController().navigate(PatientDetailsFragmentDirections.actionPatientDetailsFragmentToHomeDashboardFragment())
                    } else {
                        Toast.makeText(requireContext(), "Failed to set baseline. Please try again.", Toast.LENGTH_LONG).show()
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
