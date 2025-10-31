package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
// Note the binding class name: FragmentTreatmentPlanBinding
import com.example.asthmamanager.databinding.FragmentTreatmentPlanBinding

class TreatmentPlanFragment : Fragment() {

    private var _binding: FragmentTreatmentPlanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreatmentPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate back
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonViewPrescription.setOnClickListener {
            // Placeholder: This would launch a detailed prescription view
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}