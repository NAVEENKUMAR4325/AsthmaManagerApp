package com.example.asthmamanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

        // Handle Back Button
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Handle Download Report Button
        binding.buttonDownloadReport.setOnClickListener {
            Toast.makeText(requireContext(), "Downloading Report...", Toast.LENGTH_SHORT).show()
        }

        // Handle Logout Button
        binding.buttonLogout.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        // Fetch user profile
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMyProfile()
                }
                if (response.isSuccessful) {
                    val user = response.body()
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
