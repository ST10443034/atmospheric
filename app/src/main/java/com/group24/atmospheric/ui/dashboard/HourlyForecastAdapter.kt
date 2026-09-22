package com.group24.atmospheric.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.group24.atmospheric.databinding.ItemHourlyForecastBinding
import com.group24.atmospheric.domain.model.HourlyForecast
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HourlyForecastAdapter(private var items: List<HourlyForecast>) :
    RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun submitList(newItems: List<HourlyForecast>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], timeFormatter)
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val binding: ItemHourlyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HourlyForecast, formatter: DateTimeFormatter) {
            val time = Instant.ofEpochSecond(item.time).atZone(ZoneId.systemDefault())
            binding.tvHourlyTime.text = formatter.format(time)
            binding.tvHourlyTemp.text = "${item.temperature.toInt()}°"
        }
    }
}
