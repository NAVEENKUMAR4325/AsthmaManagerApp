package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
// Note the binding class name: FragmentReportsBinding
import com.example.asthmamanager.databinding.FragmentReportsBinding

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate back
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // On Day 3, we wire up the PDF/CSV export and toggle sharing
        binding.buttonExportPDF.setOnClickListener { /* Placeholder logic */ }
        binding.toggleSharing.setOnClickListener { /* Placeholder logic */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}