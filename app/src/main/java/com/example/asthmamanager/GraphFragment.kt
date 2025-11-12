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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asthmamanager.adapter.SymptomAdapter
import com.example.asthmamanager.databinding.FragmentGraphBinding
import com.example.asthmamanager.network.PEFRRecord
import com.example.asthmamanager.network.RetrofitClient
import com.example.asthmamanager.network.Symptom
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val args: GraphFragmentArgs by navArgs()
    private var patientId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        patientId = args.patientId
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup the title
        if (patientId == -1) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "My PEFR Graph"
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Patient Graph (ID: $patientId)"
        }

        // Setup the adapter
        binding.recyclerViewSymptoms.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSymptoms.adapter = SymptomAdapter(emptyList())

        // Setup the View History button
        binding.buttonViewHistory.setOnClickListener {
            val action = GraphFragmentDirections.actionGraphFragmentToHistoryListFragment(patientId)
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    private fun fetchData() {
        // Show progress bar and hide content
        binding.progressBar.isVisible = true
        binding.lineChart.isVisible = false
        binding.recyclerViewSymptoms.isVisible = false
        binding.noDataText.isVisible = false

        lifecycleScope.launch {
            try {
                // --- 1. Fetch PEFR Data ---
                val pefrResponse = if (patientId == -1) {
                    RetrofitClient.apiService.getMyPefrRecords()
                } else {
                    RetrofitClient.apiService.getPatientPefrRecords(patientId)
                }

                if (pefrResponse.isSuccessful && pefrResponse.body() != null) {
                    val pefrData = pefrResponse.body()!!
                    if (pefrData.isNotEmpty()) {
                        setupChart(pefrData)
                        binding.lineChart.isVisible = true
                    } else {
                        binding.lineChart.isVisible = false
                        binding.noDataText.isVisible = true
                        Log.w("GraphFragment", "No PEFR data found")
                    }
                } else {
                    handleApiError("PEFR", pefrResponse.message())
                }

                // --- 2. Fetch Symptom Data ---
                val symptomResponse = if (patientId == -1) {
                    RetrofitClient.apiService.getMySymptomRecords()
                } else {
                    RetrofitClient.apiService.getPatientSymptomRecords(patientId)
                }

                if (symptomResponse.isSuccessful && symptomResponse.body() != null) {
                    val symptomData = symptomResponse.body()!!
                    val symptomStrings = symptomData.mapNotNull { formatSymptom(it) }

                    if (symptomStrings.isNotEmpty()) {
                        setupSymptomList(symptomStrings)
                        binding.recyclerViewSymptoms.isVisible = true
                    } else {
                        binding.recyclerViewSymptoms.isVisible = false
                        Log.w("GraphFragment", "No symptom data found")
                    }
                } else {
                    handleApiError("Symptom", symptomResponse.message())
                }

            } catch (e: Exception) {
                Log.e("GraphFragment", "Network Exception: ${e.message}", e)
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.lineChart.isVisible = false
                binding.noDataText.isVisible = true
                binding.noDataText.text = "Network error. Could not load data."
            } finally {
                binding.progressBar.isVisible = false
            }
        }
    }

    // Helper to format a Symptom object into a String
    private fun formatSymptom(s: Symptom): String? {
        val parts = mutableListOf<String>()
        s.wheezeRating?.let { if (it > 0) parts.add("Wheeze: $it") }
        s.coughRating?.let { if (it > 0) parts.add("Cough: $it") }
        s.dyspneaRating?.let { if (it > 0) parts.add("Dyspnea: $it") }
        s.nightSymptomsRating?.let { if (it > 0) parts.add("Night: $it") }

        if(parts.isEmpty()) return null

        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val dateString = sdf.format(s.recordedAt)

        return "$dateString: ${parts.joinToString(", ")}"
    }

    // --- THIS IS THE MISSING FUNCTION ---
    private fun handleApiError(type: String, message: String) {
        Log.e("GraphFragment", "Error fetching $type: $message")
        Toast.makeText(context, "Error fetching $type data", Toast.LENGTH_SHORT).show()
        if (type == "PEFR") {
            binding.lineChart.visibility = View.GONE
            binding.noDataText.visibility = View.VISIBLE
            binding.noDataText.text = "Error loading PEFR data."
        }
    }
    // --- END OF NEW FUNCTION ---

    private fun setupChart(pefrRecords: List<PEFRRecord>) {
        val entries = mutableListOf<Entry>()
        val xLabels = mutableListOf<String>()
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

        pefrRecords.sortedBy { it.recordedAt }.forEachIndexed { index, record ->
            entries.add(Entry(index.toFloat(), record.pefrValue.toFloat()))
            xLabels.add(sdf.format(record.recordedAt))
        }

        val dataSet = LineDataSet(entries, "Patient PEFR")
        styleDataSet(dataSet)
        binding.lineChart.data = LineData(dataSet)

        binding.lineChart.apply {
            description.isEnabled = false
            legend.textColor = Color.BLACK
            axisLeft.textColor = Color.BLACK
            xAxis.textColor = Color.BLACK
            xAxis.setDrawLabels(true)
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false
            xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xLabels)
            xAxis.granularity = 1f
        }

        binding.lineChart.invalidate() // Refresh the chart
    }

    private fun setupSymptomList(symptoms: List<String>) {
        val adapter = SymptomAdapter(symptoms)
        binding.recyclerViewSymptoms.adapter = adapter
    }

    private fun styleDataSet(dataSet: LineDataSet) {
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.BLUE)
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}