package com.example.asthmamanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.asthmamanager.databinding.ItemMedicationBinding
import com.example.asthmamanager.network.Medication

class MedicationAdapter(
    private val medications: List<Medication>,
    private val onEditClicked: (Medication) -> Unit,
    private val onDeleteClicked: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.ViewHolder>() {

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
            binding.textMedicationName.text = medication.name
            binding.textMedicationDose.text = medication.dose
            binding.buttonEdit.setOnClickListener { onEditClicked(medication) }
            binding.buttonDelete.setOnClickListener { onDeleteClicked(medication) }
        }
    }
}
