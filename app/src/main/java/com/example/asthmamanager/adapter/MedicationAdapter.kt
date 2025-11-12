package com.example.asthmamanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.asthmamanager.databinding.ItemMedicationBinding
import com.example.asthmamanager.network.Medication

class MedicationAdapter(
    // 1. Changed to 'var' so the list can be updated
    private var medications: List<Medication>,
    private val onEditClicked: (Medication) -> Unit,
    private val onDeleteClicked: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.ViewHolder>() {

    // 2. ADD THIS NEW FUNCTION
    /**
     * Updates the adapter's list with new data and refreshes the RecyclerView.
     */
    fun updateData(newMedications: List<Medication>) {
        medications = newMedications
        notifyDataSetChanged() // Tell the RecyclerView to refresh
    }
    // --- END OF NEW FUNCTION ---

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onEditClicked, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(medications[position])
    }

    override fun getItemCount() = medications.size

    class ViewHolder(
        private val binding: ItemMedicationBinding,
        private val onEditClicked: (Medication) -> Unit,
        private val onDeleteClicked: (Medication) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication) {
            // This matches your item_medication.xml
            binding.textMedicationName.text = medication.name
            binding.textMedicationDose.text = medication.dose ?: "No dose specified"
            binding.buttonEdit.setOnClickListener { onEditClicked(medication) }
            binding.buttonDelete.setOnClickListener { onDeleteClicked(medication) }
        }
    }
}