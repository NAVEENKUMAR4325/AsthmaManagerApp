package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asthmamanager.adapter.EmergencyContactAdapter
import com.example.asthmamanager.adapter.MedicationAdapter
import com.example.asthmamanager.databinding.FragmentProfileBinding
import com.example.asthmamanager.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSaveChanges.setOnClickListener {
            showSaveChangesConfirmationDialog()
        }

        binding.buttonDownloadReport.setOnClickListener {
            Toast.makeText(requireContext(), "Downloading Report...", Toast.LENGTH_SHORT).show()
        }

        binding.buttonLogout.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        fetchProfileData()
    }

    private fun fetchProfileData() {
        lifecycleScope.launch {
            try {
                val profileResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMyProfile()
                }
                if (profileResponse.isSuccessful) {
                    val user = profileResponse.body()
                    binding.editTextName.setText(user?.fullName)
                    binding.editTextEmail.setText(user?.email)
                    binding.editTextContact.setText(user?.contactInfo)
                    binding.editTextAddress.setText(user?.address)
                    binding.editTextAge.setText(user?.age?.toString())
                    binding.editTextHeight.setText(user?.height?.toString())
                    binding.editTextGender.setText(user?.gender)
                    binding.editTextBaselinePEFR.setText(user?.baselinePefr?.toString())
                } else {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }

                val contactsResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getEmergencyContacts()
                }
                if (contactsResponse.isSuccessful) {
                    val contacts = contactsResponse.body() ?: emptyList()
                    binding.recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerViewContacts.adapter = EmergencyContactAdapter(contacts, { /* TODO: Handle edit */ }, { /* TODO: Handle delete */ })
                }

                val medicationsResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMedications()
                }
                if (medicationsResponse.isSuccessful) {
                    val medications = medicationsResponse.body() ?: emptyList()
                    binding.recyclerViewMedications.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerViewMedications.adapter = MedicationAdapter(medications, { /* TODO: Handle edit */ }, { /* TODO: Handle delete */ })
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSaveChangesConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Save Changes")
            .setMessage("Are you sure you want to save these changes?")
            .setPositiveButton("Save") { _, _ ->
                // TODO: Implement save logic
                Toast.makeText(requireContext(), "Changes saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
