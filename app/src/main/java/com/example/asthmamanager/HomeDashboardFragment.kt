package com.example.asthmamanager

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.asthmamanager.databinding.FragmentHomeDashboardBinding
import com.example.asthmamanager.network.RetrofitClient
import com.example.asthmamanager.network.SymptomCreate
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to Profile
        binding.imageProfile.setOnClickListener {
            findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToProfileFragment())
        }

        // Navigate to PEFR Input screen
        binding.buttonRecordPEFR.setOnClickListener {
            findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToPEFRInputFragment())
        }

        // Navigate to Notification screen
        binding.buttonSetReminder.setOnClickListener {
            findNavController().navigate(HomeDashboardFragmentDirections.actionHomeDashboardFragmentToNotificationFragment())
        }

        // Set up the toggle buttons
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.buttonWeekly -> showWeeklyData()
                    R.id.buttonMonthly -> showMonthlyData()
                }
            }
        }

        fetchDashboardData()
        logSymptoms()
    }

    private fun fetchDashboardData() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMyProfile()
                }
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        binding.textViewHeader.text = "Welcome, ${it.fullName}"
                        it.baselinePefr?.let {
                            binding.textBaselinePEFRValue.text = it.toString()
                            setupChart(it)
                            updateTreatmentNotification(450, it) // Using a sample value for now
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun setupChart(baselinePefr: Int) {
        val redZone = baselinePefr * 0.5f
        val yellowZone = baselinePefr * 0.8f

        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.setDrawGridLines(false)
            xAxis.setDrawLabels(false)
            axisLeft.setDrawGridLines(false)
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false

            axisLeft.addLimitLine(LimitLine(redZone, "Red Zone").apply {
                lineWidth = 2f
                lineColor = Color.RED
            })
            axisLeft.addLimitLine(LimitLine(yellowZone, "Yellow Zone").apply {
                lineWidth = 2f
                lineColor = Color.YELLOW
            })
        }
        showWeeklyData()
    }

    private fun showWeeklyData() {
        val entries = getWeeklySampleData()
        val dataSet = LineDataSet(entries, "Weekly PEFR")
        styleDataSet(dataSet)
        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.invalidate()
    }

    private fun showMonthlyData() {
        val entries = getMonthlySampleData()
        val dataSet = LineDataSet(entries, "Monthly PEFR")
        styleDataSet(dataSet)
        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.invalidate()
    }

    private fun styleDataSet(dataSet: LineDataSet) {
        dataSet.color = Color.WHITE
        dataSet.valueTextColor = Color.WHITE
        dataSet.setCircleColor(Color.WHITE)
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false)
    }

    private fun getWeeklySampleData(): List<Entry> {
        return listOf(
            Entry(0f, 450f),
            Entry(1f, 460f),
            Entry(2f, 440f),
            Entry(3f, 470f),
            Entry(4f, 455f),
            Entry(5f, 465f),
            Entry(6f, 450f)
        )
    }

    private fun getMonthlySampleData(): List<Entry> {
        return listOf(
            Entry(0f, 450f),
            Entry(1f, 460f),
            Entry(2f, 440f),
            Entry(3f, 470f),
            Entry(4f, 455f),
            Entry(5f, 465f),
            Entry(6f, 450f),
            Entry(7f, 455f),
            Entry(8f, 460f),
            Entry(9f, 470f),
            Entry(10f, 465f),
            Entry(11f, 455f),
            Entry(12f, 460f),
            Entry(13f, 450f),
            Entry(14f, 440f),
            Entry(15f, 450f),
            Entry(16f, 460f),
            Entry(17f, 455f),
            Entry(18f, 465f),
            Entry(19f, 470f),
            Entry(20f, 450f),
            Entry(21f, 460f),
            Entry(22f, 455f),
            Entry(23f, 440f),
            Entry(24f, 460f),
            Entry(25f, 450f),
            Entry(26f, 465f),
            Entry(27f, 455f),
            Entry(28f, 470f),
            Entry(29f, 460f),
            Entry(30f, 450f)
        )
    }

    private fun updateTreatmentNotification(pefrValue: Int, baselinePefr: Int) {
        val zone = getPEFRZone(pefrValue, baselinePefr)
        val advice = when (zone) {
            "Green Zone" -> "Continue the inhaler - Controller"
            "Yellow Zone" -> "Step up dose / Use reliever"
            "Red Zone" -> "Emergency hospital visit"
            else -> ""
        }
        binding.textZoneGuidance.text = advice
    }

    private fun getPEFRZone(pefrValue: Int, baselinePefr: Int): String {
        val percentage = (pefrValue.toFloat() / baselinePefr) * 100
        return when {
            percentage >= 80 -> "Green Zone"
            percentage >= 50 -> "Yellow Zone"
            else -> "Red Zone"
        }
    }

    private fun logSymptoms() {
        val wheezeRating = binding.ratingWheeze.rating.toInt()
        val coughRating = binding.ratingCough.rating.toInt()
        val dyspneaRating = binding.ratingDyspnea.rating.toInt()
        val nightSymptomsRating = binding.ratingNightSymptoms.rating.toInt()

        val symptomRequest = SymptomCreate(
            wheezeRating = wheezeRating,
            coughRating = coughRating,
            dyspneaRating = dyspneaRating,
            nightSymptomsRating = nightSymptomsRating,
            dustExposure = null,
            smokeExposure = null,
            severity = null,
            onsetAt = null,
            duration = null,
            suspectedTrigger = null
        )

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.recordSymptom(symptomRequest)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
