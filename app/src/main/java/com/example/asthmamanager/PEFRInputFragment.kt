package com.example.asthmamanager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.asthmamanager.databinding.FragmentPefrInputBinding
import com.example.asthmamanager.network.PEFRRecordCreate
import com.example.asthmamanager.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PEFRInputFragment : Fragment() {

    private var _binding: FragmentPefrInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPefrInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Record PEFR"

        binding.buttonSubmit.setOnClickListener {
            val pefrValue = binding.editTextPEFR.text.toString().toIntOrNull()

            if (pefrValue == null) {
                Toast.makeText(requireContext(), "Please enter a valid PEFR value", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pefrRequest = PEFRRecordCreate(pefrValue)

            // --- Disable button to prevent double-click ---
            binding.buttonSubmit.isEnabled = false
            binding.buttonSubmit.text = "Saving..."

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.recordPEFR(pefrRequest)
                    }

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "PEFR recorded successfully", Toast.LENGTH_SHORT).show()

                        // --- THIS IS THE FIX ---
                        // We now use the action that starts from THIS fragment
                        findNavController().navigate(R.id.action_PEFRInputFragment_to_symptomTrackerFragment)
                        // --- END OF FIX ---

                    } else {
                        // This block will catch 401, 500, etc.
                        val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("PEFRInputFragment", "API Error: ${response.code()} - $errorMsg")
                        Toast.makeText(requireContext(), "Failed to record PEFR. Please try again.", Toast.LENGTH_LONG).show()
                        binding.buttonSubmit.isEnabled = true // Re-enable on error
                        binding.buttonSubmit.text = "Submit" // Reset text on error
                    }
                } catch (e: Exception) {
                    // This block catches navigation errors or JSON parsing errors
                    Log.e("PEFRInputFragment", "Navigation/Parsing Error: ${e.message}", e)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.buttonSubmit.isEnabled = true // Re-enable on error
                    binding.buttonSubmit.text = "Submit" // Reset text on error
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}