package com.example.asthmamanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.asthmamanager.databinding.ItemPatientCardBinding

class PatientAdapter(
    private val patients: List<Patient>,
    private val onPatientClicked: (Patient) -> Unit,
    private val onDownloadClicked: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatientViewHolder(binding, onPatientClicked, onDownloadClicked)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }

    override fun getItemCount() = patients.size

    class PatientViewHolder(
        private val binding: ItemPatientCardBinding,
        private val onPatientClicked: (Patient) -> Unit,
        private val onDownloadClicked: (Patient) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(patient: Patient) {
            binding.textPatientName.text = patient.name
            binding.textPatientZone.text = patient.zone
            binding.textLatestPEFR.text = patient.latestPEFR.toString()
            binding.textSymptomSeverity.text = patient.symptomSeverity

            itemView.setOnClickListener {
                onPatientClicked(patient)
            }

            binding.buttonDownloadReport.setOnClickListener {
                onDownloadClicked(patient)
            }
        }
    }
}
