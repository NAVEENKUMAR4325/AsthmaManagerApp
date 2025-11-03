package com.example.asthmamanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.asthmamanager.databinding.ItemPatientCardBinding
import com.example.asthmamanager.network.User

class PatientAdapter(
    private val patients: List<User>,
    private val onPatientClicked: (User) -> Unit,
    private val onDownloadClicked: (User) -> Unit
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
        private val onPatientClicked: (User) -> Unit,
        private val onDownloadClicked: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(patient: User) {
            binding.textPatientName.text = patient.fullName ?: "N/A"
            binding.textPatientZone.text = "Unknown Zone" // This will be updated later
            binding.textLatestPEFR.text = patient.baseline?.baselineValue?.toString() ?: "N/A"
            binding.textSymptomSeverity.text = "N/A" // This will be updated later

            itemView.setOnClickListener {
                onPatientClicked(patient)
            }

            binding.buttonDownloadReport.setOnClickListener {
                onDownloadClicked(patient)
            }
        }
    }
}
