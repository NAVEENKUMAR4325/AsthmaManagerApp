package com.example.asthmamanager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asthmamanager.adapter.PatientAdapter
import com.example.asthmamanager.databinding.FragmentDoctorDashboardBinding
import com.example.asthmamanager.network.RetrofitClient
import com.example.asthmamanager.network.User
import kotlinx.coroutines.launch

class DoctorDashboardFragment : Fragment() {

    private var _binding: FragmentDoctorDashboardBinding? = null
    private val binding get() = _binding!!

    // Hold a reference to the adapter
    private lateinit var patientAdapter: PatientAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to Doctor's own profile
        binding.imageProfile.setOnClickListener {
            findNavController().navigate(DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToDoctorProfileFragment())
        }

        // Setup the RecyclerView and Adapter (with an empty list)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        // Fetch data every time the fragment is shown
        fetchPatients()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter once with an empty list and all 3 click listeners
        patientAdapter = PatientAdapter(emptyList(), { patient ->
            // On patient clicked, navigate to graph with patientId
            val action = DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToGraphFragment(patient.id)
            findNavController().navigate(action)
        }, { patient ->
            // On download clicked, navigate to reports with patientId
            val action = DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToReportsFragment(patient.id)
            findNavController().navigate(action)
        }, { patient ->
            // On prescribe clicked, navigate to prescribe medication with patientId
            val action = DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToPrescribeMedicationFragment(patient.id)
            findNavController().navigate(action)
        })

        binding.recyclerViewPatients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPatients.adapter = patientAdapter
    }

    private fun fetchPatients() {
        binding.progressBar.isVisible = true
        binding.recyclerViewPatients.isVisible = false
        binding.textNoPatients.isVisible = false

        lifecycleScope.launch {
            try {
                // Call the API (using null for no search/filter)
                val response = RetrofitClient.apiService.getDoctorPatients(null, null)

                if (response.isSuccessful) {
                    val patients = response.body()
                    if (patients.isNullOrEmpty()) {
                        binding.textNoPatients.isVisible = true
                    } else {
                        // Update the adapter with the new list
                        patientAdapter.updateData(patients) // Assumes PatientAdapter has updateData()
                        binding.recyclerViewPatients.isVisible = true
                    }
                } else {
                    // Handle API error
                    val errorMsg = response.errorBody()?.string() ?: getString(R.string.failed_to_load_data_error)
                    Log.e("DoctorDashboard", "API Error: $errorMsg")
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    binding.textNoPatients.text = getString(R.string.failed_to_load_data_error)
                    binding.textNoPatients.isVisible = true
                }

            } catch (e: Exception) {
                // Handle network failure
                Log.e("DoctorDashboard", "Network Exception: ${e.message}", e)
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.textNoPatients.text = getString(R.string.network_error_loading_data)
                binding.textNoPatients.isVisible = true
            } finally {
                binding.progressBar.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}