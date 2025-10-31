package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asthmamanager.databinding.FragmentDoctorDashboardBinding

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

        // Set up the RecyclerView
        val patients = createSamplePatients()
        val adapter = PatientAdapter(patients, {
            // On patient clicked
            findNavController().navigate(DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToGraphFragment())
        }, {
            // On download clicked
            findNavController().navigate(DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToReportsFragment())
        })
        binding.recyclerViewPatients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPatients.adapter = adapter
    }

    private fun createSamplePatients(): List<Patient> {
        return listOf(
            Patient("John Doe", "Red Zone", 250, "High (8/10)"),
            Patient("Jane Smith", "Yellow Zone", 450, "Medium (5/10)"),
            Patient("Peter Jones", "Green Zone", 600, "Low (2/10)")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}