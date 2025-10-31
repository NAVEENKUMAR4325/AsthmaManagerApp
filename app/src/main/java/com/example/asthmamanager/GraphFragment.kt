package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
// Note the binding class name: FragmentGraphBinding
import com.example.asthmamanager.databinding.FragmentGraphBinding

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate back when toolbar icon is clicked
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Logic for "View History" button would go here
        binding.buttonViewHistory.setOnClickListener {
            // Placeholder: maybe navigate to a list view
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}