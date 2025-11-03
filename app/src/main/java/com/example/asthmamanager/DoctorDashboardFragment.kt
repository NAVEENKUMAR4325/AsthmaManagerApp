package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asthmamanager.databinding.FragmentDoctorDashboardBinding
import com.example.asthmamanager.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorDashboardFragment : Fragment() {

    private var _binding: FragmentDoctorDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate back
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Navigate to Doctor's own profile
        binding.imageProfile.setOnClickListener {
            findNavController().navigate(DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToProfileFragment())
        }

        // Fetch patients
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getDoctorPatients(null, null)
                }
                if (response.isSuccessful) {
                    val patients = response.body() ?: emptyList()
                    val adapter = PatientAdapter(patients, {
                        // On patient clicked
                        findNavController().navigate(DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToGraphFragment())
                    }, {
                        // On download clicked
                        findNavController().navigate(DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToReportsFragment())
                    })
                    binding.recyclerViewPatients.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerViewPatients.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "Failed to load patients", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
