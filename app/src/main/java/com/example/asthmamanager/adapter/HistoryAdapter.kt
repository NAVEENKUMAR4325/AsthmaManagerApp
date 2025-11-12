package com.example.asthmamanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.asthmamanager.databinding.ItemHistoryRecordBinding
import com.example.asthmamanager.network.PEFRRecord
import com.example.asthmamanager.network.Symptom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// A sealed class to hold either type of record
sealed class HistoryItem {
    data class Pefr(val record: PEFRRecord) : HistoryItem()
    data class Sym(val record: Symptom) : HistoryItem()
    fun getDate(): Date {
        return when (this) {
            is Pefr -> this.record.recordedAt
            is Sym -> this.record.recordedAt
        }
    }
}

class HistoryAdapter(
    private var historyItems: List<HistoryItem>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun updateData(newItems: List<HistoryItem>) {
        // Sort the combined list by date, descending
        historyItems = newItems.sortedByDescending { it.getDate() }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyItems[position])
    }

    override fun getItemCount(): Int = historyItems.size

    inner class HistoryViewHolder(private val binding: ItemHistoryRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        fun bind(item: HistoryItem) {
            when (item) {
                is HistoryItem.Pefr -> {
                    val record = item.record
                    binding.textRecordType.text = "PEFR Record"
                    binding.textRecordDate.text = dateFormatter.format(record.recordedAt)
                    binding.textRecordDetails.text =
                        "Value: ${record.pefrValue} (${record.zone} Zone)"
                }
                is HistoryItem.Sym -> {
                    val record = item.record
                    binding.textRecordType.text = "Symptom Record"
                    binding.textRecordDate.text = dateFormatter.format(record.recordedAt)
                    binding.textRecordDetails.text = formatSymptomDetails(record)
                }
            }
        }

        private fun formatSymptomDetails(s: Symptom): String {
            val parts = mutableListOf<String>()
            s.wheezeRating?.let { if (it > 0) parts.add("Wheeze: $it") }
            s.coughRating?.let { if (it > 0) parts.add("Cough: $it") }
            s.dyspneaRating?.let { if (it > 0) parts.add("Dyspnea: $it") }
            s.nightSymptomsRating?.let { if (it > 0) parts.add("Night: $it") }

            if (parts.isEmpty()) return "Symptoms reported (Mild)"
            return parts.joinToString(", ")
        }
    }
}