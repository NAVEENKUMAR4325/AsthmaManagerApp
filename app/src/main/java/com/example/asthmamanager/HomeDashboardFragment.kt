package com.example.asthmamanager

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.asthmamanager.databinding.FragmentHomeDashboardBinding
import com.example.asthmamanager.network.PEFRRecord
import com.example.asthmamanager.network.RetrofitClient
import com.example.asthmamanager.network.Symptom
import com.example.asthmamanager.network.User
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeDashboardFragment : Fragment() {

    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Fetch data every time the screen is viewed to keep it fresh
        fetchDashboardData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Navigation ---
        binding.imageProfile.setOnClickListener {
            findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToProfileFragment())
        }
        binding.buttonRecordPEFR.setOnClickListener {
            findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToPEFRInputFragment())
        }
        binding.buttonSetReminder.setOnClickListener {
            // Your nav graph has this pointing to NotificationFragment, which is fine
            findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToNotificationFragment())
        }
        binding.cardGraph.setOnClickListener {
            // Also navigate to graph when the chart is clicked
            findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToGraphFragment())
        }

        // This button in your XML navigates to the Treatment Plan
        binding.cardTodayZone.setOnClickListener {
            findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToTreatmentPlanFragment())
        }

        // --- Toggle ---
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                // This will re-fetch data for the chart
                fetchChartData(isWeekly = (checkedId == R.id.buttonWeekly))
            }
        }
    }

    private fun fetchDashboardData() {
        binding.progressBar.isVisible = true
        binding.textViewError.isVisible = false
        binding.contentScrollView.isVisible = false

        lifecycleScope.launch {
            try {
                // This now returns the User object with the new 'latest' fields
                val response = RetrofitClient.apiService.getMyProfile()
                binding.progressBar.isVisible = false

                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        binding.contentScrollView.isVisible = true
                        updateUI(user)
                    } else {
                        binding.textViewError.text = "Could not retrieve user profile."
                        binding.textViewError.isVisible = true
                    }
                } else {
                    binding.textViewError.text = "Error: ${response.message()}"
                    binding.textViewError.isVisible = true
                }
            } catch (e: Exception) {
                binding.progressBar.isVisible = false
                binding.textViewError.text = "Network error. Please check your connection."
                binding.textViewError.isVisible = true
                Log.e("HomeDashboard", "Network Exception: ${e.message}", e)
            }
        }
    }

    private fun updateUI(user: User) {
        binding.textViewHeader.text = "Welcome, ${user.fullName ?: "User"}"

        // --- 1. Update Baseline Card ---
        val baseline = user.baseline?.baselineValue
        if (baseline != null) {
            binding.textBaselinePEFRValue.text = baseline.toString()
            setupChartLimits(baseline) // Setup chart zones
        } else {
            binding.textBaselinePEFRValue.text = "N/A"
            binding.lineChart.clear()
            binding.lineChart.invalidate()
            // Prompt user to set baseline
            binding.textBaselinePEFRValue.setOnClickListener {
                Toast.makeText(context, "Please set your baseline PEFR in your Profile.", Toast.LENGTH_LONG).show()
                findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToProfileFragment())
            }
        }

        // --- 2. Update Today's Zone Card ---
        val latestPefr = user.latestPefrRecord
        if (latestPefr != null) {
            binding.textPEFRValue.text = latestPefr.pefrValue.toString()
            binding.textPEFRPercentage.text = "(${latestPefr.percentage?.toInt() ?: 0}%)"
            binding.textTrendIndicator.text = latestPefr.trend?.replaceFirstChar { it.uppercase() } ?: "Stable"
            binding.textLastRecorded.text = "Last recorded: ${formatDate(latestPefr.recordedAt)}"
            updateZoneUI(latestPefr.zone)
        } else {
            // No PEFR data yet
            binding.textPEFRValue.text = "---"
            binding.textPEFRPercentage.text = ""
            binding.textTrendIndicator.text = "No Data"
            binding.textLastRecorded.text = "No PEFR recorded yet"
            updateZoneUI("Unknown")
        }

        // --- 3. Update Symptoms Card ---
        val latestSymptom = user.latestSymptom
        if (latestSymptom != null) {
            binding.ratingWheeze.rating = latestSymptom.wheezeRating?.toFloat() ?: 0f
            binding.ratingCough.rating = latestSymptom.coughRating?.toFloat() ?: 0f
            binding.ratingDyspnea.rating = latestSymptom.dyspneaRating?.toFloat() ?: 0f
            binding.ratingNightSymptoms.rating = latestSymptom.nightSymptomsRating?.toFloat() ?: 0f
        } else {
            // No symptoms recorded
            binding.ratingWheeze.rating = 0f
            binding.ratingCough.rating = 0f
            binding.ratingDyspnea.rating = 0f
            binding.ratingNightSymptoms.rating = 0f
        }

        // --- 4. Fetch Chart Data (Default to Weekly) ---
        binding.toggleGroup.check(R.id.buttonWeekly) // Ensure weekly is checked
        fetchChartData(isWeekly = true)
    }

    private fun setupChartLimits(baselinePefr: Int) {
        val redZone = baselinePefr * 0.5f
        val yellowZone = baselinePefr * 0.8f

        val axis = binding.lineChart.axisLeft
        axis.removeAllLimitLines() // Clear old limits
        axis.addLimitLine(LimitLine(redZone, "Red Zone").apply {
            lineWidth = 2f
            lineColor = Color.RED
            textColor = Color.WHITE
        })
        axis.addLimitLine(LimitLine(yellowZone, "Yellow Zone").apply {
            lineWidth = 2f
            lineColor = Color.YELLOW
            textColor = Color.WHITE
        })
        axis.axisMinimum = 0f // Start Y-axis at 0
        axis.setDrawGridLines(false)
        axis.textColor = Color.WHITE

        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.setDrawGridLines(false)
            xAxis.setDrawLabels(false)
            axisRight.isEnabled = false
        }
    }

    private fun fetchChartData(isWeekly: Boolean) {
        lifecycleScope.launch {
            try {
                // --- This now fetches REAL data ---
                val response = RetrofitClient.apiService.getMyPefrRecords()
                if (response.isSuccessful && response.body() != null) {
                    val records = response.body()!!

                    val entries = if (isWeekly) {
                        // Get the last 7 records
                        records.takeLast(7).mapIndexed { i, record -> Entry(i.toFloat(), record.pefrValue.toFloat()) }
                    } else {
                        // Get the last 30 records for "Monthly"
                        records.takeLast(30).mapIndexed { i, record -> Entry(i.toFloat(), record.pefrValue.toFloat()) }
                    }

                    if(entries.isNotEmpty()) {
                        val dataSet = LineDataSet(entries, if(isWeekly) "Weekly PEFR" else "Monthly PEFR")
                        styleDataSet(dataSet)
                        binding.lineChart.data = LineData(dataSet)
                    } else {
                        binding.lineChart.clear() // No data, clear the chart
                    }

                } else {
                    Log.e("HomeDashboard", "Failed to fetch chart data")
                    binding.lineChart.clear()
                }
            } catch (e: Exception) {
                Log.e("HomeDashboard", "Chart data exception: ${e.message}", e)
                binding.lineChart.clear()
            } finally {
                binding.lineChart.invalidate() // Refresh the chart
            }
        }
    }

    private fun styleDataSet(dataSet: LineDataSet) {
        dataSet.color = Color.WHITE
        dataSet.valueTextColor = Color.WHITE
        dataSet.setCircleColor(Color.WHITE)
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2.5f
    }

    private fun updateZoneUI(zone: String) {
        // You must add these colors to your colors.xml
        val green = ContextCompat.getColor(requireContext(), R.color.greenZone)
        val yellow = ContextCompat.getColor(requireContext(), R.color.yellowZone)
        val red = ContextCompat.getColor(requireContext(), R.color.redZone)
        val grey = ContextCompat.getColor(requireContext(), R.color.cardLightBackgroundColor) // Use a default

        when (zone) {
            "Green" -> {
                binding.cardTodayZone.setCardBackgroundColor(green)
                binding.textZoneGuidance.text = "You are in the Green Zone. Continue your regular plan."
            }
            "Yellow" -> {
                binding.cardTodayZone.setCardBackgroundColor(yellow)
                binding.textZoneGuidance.text = "Yellow Zone: Use your reliever inhaler. Follow your action plan."
            }
            "Red" -> {
                binding.cardTodayZone.setCardBackgroundColor(red)
                binding.textZoneGuidance.text = "Red Zone: Medical Emergency. Use reliever and seek help."
            }
            else -> { // Unknown
                binding.cardTodayZone.setCardBackgroundColor(grey)
                binding.textZoneGuidance.text = "Record a PEFR value to see your current zone."
            }
        }
    }

    private fun formatDate(date: Date): String {
        return try {
            // Format as "12 Nov, 10:30 AM"
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            sdf.format(date)
        } catch (e: Exception) {
            "just now"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}