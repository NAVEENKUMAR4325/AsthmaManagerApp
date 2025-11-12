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
import com.example.asthmamanager.databinding.FragmentSymptomTrackerBinding
import com.example.asthmamanager.network.RetrofitClient
import com.example.asthmamanager.network.SymptomCreate
import kotlinx.coroutines.launch
import java.util.Date

class SymptomTrackerFragment : Fragment() {

    private var _binding: FragmentSymptomTrackerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSymptomTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Symptom Tracker"

        // Set click listener for the submit button
        binding.btnSubmitSymptoms.setOnClickListener {
            submitSymptoms()
        }
    }

    private fun submitSymptoms() {

        // Read values from all the new UI elements
        val wheezeRating = binding.ratingWheeze.rating.toInt()
        val coughRating = binding.ratingCough.rating.toInt()
        val dyspneaRating = binding.ratingDyspnea.rating.toInt()
        val nightSymptomsRating = binding.ratingNightSymptoms.rating.toInt()
        val dustExposure = binding.checkDust.isChecked
        val smokeExposure = binding.checkSmoke.isChecked
        val trigger = binding.etSuspectedTrigger.text.toString()

        // Combine all ratings to determine a general severity
        val totalScore = wheezeRating + coughRating + dyspneaRating + nightSymptomsRating
        val severity = when {
            totalScore == 0 -> "None"
            totalScore <= 4 -> "Mild"
            totalScore <= 10 -> "Moderate"
            else -> "Severe"
        }

        // Create the request object
        val symptomRequest = SymptomCreate(
            wheezeRating = wheezeRating,
            coughRating = coughRating,
            dyspneaRating = dyspneaRating,
            nightSymptomsRating = nightSymptomsRating,
            dustExposure = dustExposure,
            smokeExposure = smokeExposure,
            severity = severity, // Set a calculated severity
            onsetAt = Date(), // Use current time
            duration = null, // Not captured in this form
            suspectedTrigger = trigger.ifBlank { null } // Set trigger if not blank
        )

        binding.btnSubmitSymptoms.isEnabled = false
        binding.btnSubmitSymptoms.text = "Saving..."

        lifecycleScope.launch {
            try {
                // Call the API
                val response = RetrofitClient.apiService.recordSymptom(symptomRequest)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Symptoms saved successfully", Toast.LENGTH_SHORT).show()
                    // Pop back to the home dashboard, completing the flow
                    findNavController().popBackStack(R.id.homeDashboardFragment, false)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("SymptomTracker", "API Error: $errorMsg")
                    Toast.makeText(context, "Error saving: $errorMsg", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("SymptomTracker", "Network Exception: ${e.message}", e)
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Re-enable the button
                binding.btnSubmitSymptoms.isEnabled = true
                binding.btnSubmitSymptoms.text = "Submit Symptoms"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}