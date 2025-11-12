package com.example.asthmamanager

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asthmamanager.adapter.MedicationAdapter
import com.example.asthmamanager.databinding.FragmentTreatmentPlanBinding
import com.example.asthmamanager.network.RetrofitClient
import com.example.asthmamanager.network.User
import kotlinx.coroutines.launch

class TreatmentPlanFragment : Fragment() {

    private var _binding: FragmentTreatmentPlanBinding? = null
    private val binding get() = _binding!!

    // Adapter for the medication list
    private lateinit var medicationAdapter: MedicationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreatmentPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Your Treatment Plan"

        // Setup the RecyclerView
        setupRecyclerView()

        binding.buttonViewPrescription.setOnClickListener {
            // Placeholder: This would launch a detailed prescription view
            Toast.makeText(context, "Detailed prescription view not implemented.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Fetch data every time the fragment is viewed
        fetchTreatmentData()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with the click listeners
        medicationAdapter = MedicationAdapter(
            emptyList(),
            onEditClicked = { medication ->
                // TODO: Handle Edit click
                Toast.makeText(context, "Edit for ${medication.name} not implemented.", Toast.LENGTH_SHORT).show()
            },
            onDeleteClicked = { medication ->
                // TODO: Handle Delete click (call API to delete)
                Toast.makeText(context, "Delete for ${medication.name} not implemented.", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewMedications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = medicationAdapter
        }
    }

    private fun fetchTreatmentData() {
        binding.progressBar.isVisible = true
        binding.recyclerViewMedications.isVisible = false
        binding.textNoMedications.isVisible = false

        lifecycleScope.launch {
            try {
                // --- 1. Fetch Medications ---
                val medsResponse = RetrofitClient.apiService.getMedications()
                if (medsResponse.isSuccessful) {
                    val medications = medsResponse.body()
                    if (medications.isNullOrEmpty()) {
                        binding.textNoMedications.isVisible = true
                        binding.recyclerViewMedications.isVisible = false
                    } else {
                        medicationAdapter.updateData(medications)
                        binding.textNoMedications.isVisible = false
                        binding.recyclerViewMedications.isVisible = true
                    }
                } else {
                    Log.e("TreatmentPlan", "Error fetching medications")
                    binding.textNoMedications.text = "Could not load medications"
                    binding.textNoMedications.isVisible = true
                }

                // --- 2. Fetch User Profile (for Zone) ---
                val profileResponse = RetrofitClient.apiService.getMyProfile()
                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    updateZoneCard(profileResponse.body()!!)
                } else {
                    Log.e("TreatmentPlan", "Error fetching profile")
                    binding.textZoneTitle.text = "Error"
                    binding.textPlanMessage.text = "Could not load your zone status."
                }

            } catch (e: Exception) {
                Log.e("TreatmentPlan", "Network Exception: ${e.message}", e)
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.isVisible = false
            }
        }
    }

    private fun updateZoneCard(user: User) {
        // This is a placeholder until we update the /profile/me endpoint
        // to return the patient's *latest* zone.
        val baseline = user.baseline?.baselineValue
        if (baseline != null) {
            binding.textZoneTitle.text = "Your Baseline"
            binding.textPlanMessage.text = "Your personal baseline PEFR is $baseline. " +
                    "Your zones are calculated based on this value."
            binding.textZoneTitle.setTextColor(Color.WHITE)
        } else {
            binding.textZoneTitle.text = "No Baseline Set"
            binding.textPlanMessage.text = "Please go to your profile to set your baseline PEFR."
            binding.textZoneTitle.setTextColor(Color.YELLOW)
        }

        // TODO: The ideal solution is to have the /profile/me endpoint
        // also return the user's *latest_pefr_record* to get the zone.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}