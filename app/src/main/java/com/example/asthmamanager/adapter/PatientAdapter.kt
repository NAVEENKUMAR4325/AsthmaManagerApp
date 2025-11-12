package com.example.asthmamanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.asthmamanager.databinding.ItemPatientCardBinding
import com.example.asthmamanager.network.User

class PatientAdapter(
    // 1. Changed to 'var' so the list can be updated
    private var patients: List<User>,
    private val onPatientClicked: (User) -> Unit,
    private val onDownloadClicked: (User) -> Unit,
    // 2. Added the 3rd click listener to match your fragment
    private val onPrescribeClicked: (User) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // 3. Pass all three listeners to the ViewHolder
        return PatientViewHolder(binding, onPatientClicked, onDownloadClicked, onPrescribeClicked)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }

    override fun getItemCount() = patients.size

    // 4. ADDED THE 'updateData' FUNCTION
    /**
     * Updates the adapter's list with new data and refreshes the RecyclerView.
     */
    fun updateData(newPatients: List<User>) {
        patients = newPatients
        notifyDataSetChanged() // Tell the RecyclerView to refresh
    }
    // --- END OF NEW FUNCTION ---

    class PatientViewHolder(
        private val binding: ItemPatientCardBinding,
        private val onPatientClicked: (User) -> Unit,
        private val onDownloadClicked: (User) -> Unit,
        // 5. Added the 3rd click listener here too
        private val onPrescribeClicked: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: User) {
            binding.textPatientName.text = patient.fullName ?: "N/A"
            binding.textPatientZone.text = "Unknown Zone" // TODO: Update with real data
            binding.textLatestPEFR.text = patient.baseline?.baselineValue?.toString() ?: "N/A"
            binding.textSymptomSeverity.text = "N/A" // TODO: Update with real data

            // 6. Set up all three click listeners
            itemView.setOnClickListener {
                onPatientClicked(patient)
            }

            binding.buttonDownloadReport.setOnClickListener {
                onDownloadClicked(patient)
            }

            // 7. Added the prescribe click listener
            // **NOTE:** Please check your 'item_patient_card.xml' for the correct ID.
            // I am guessing the ID is 'buttonPrescribe'.
            binding.buttonPrescribe.setOnClickListener {
                onPrescribeClicked(patient)
            }
        }
    }
}