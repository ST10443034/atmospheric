package com.group24.atmospheric.ui.forecast

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.group24.atmospheric.databinding.ItemForecastDailyRowBinding
import com.group24.atmospheric.databinding.ItemForecastHourlyRowBinding
import com.group24.atmospheric.domain.model.DailyForecast
import com.group24.atmospheric.domain.model.HourlyForecast
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun conditionFor(weatherCode: Int): String = when (weatherCode) {
    0 -> "Clear"
    1, 2 -> "Partly cloudy"
    3 -> "Cloudy"
    45, 48 -> "Fog"
    in 51..67 -> "Light rain"
    in 71..77 -> "Snow"
    in 80..82 -> "Rain showers"
    in 95..99 -> "Thunderstorm"
    else -> "Unknown"
}

class HourlyRowAdapter(private var items: List<HourlyForecast> = emptyList()) :
    RecyclerView.Adapter<HourlyRowAdapter.ViewHolder>() {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun submitList(newItems: List<HourlyForecast>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemForecastHourlyRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val time = Instant.ofEpochSecond(item.time).atZone(ZoneId.systemDefault())
        holder.binding.tvTime.text = timeFormatter.format(time)
        holder.binding.tvCondition.text = conditionFor(item.weatherCode)
        holder.binding.tvPop.text = "${item.precipitationProbability}%"
        holder.binding.tvTemp.text = "${item.temperature.toInt()}°"
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemForecastHourlyRowBinding) : RecyclerView.ViewHolder(binding.root)
}

class DailyRowAdapter(private var items: List<DailyForecast> = emptyList()) :
    RecyclerView.Adapter<DailyRowAdapter.ViewHolder>() {

    private val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

    fun submitList(newItems: List<DailyForecast>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemForecastDailyRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val day = Instant.ofEpochSecond(item.date).atZone(ZoneId.systemDefault())
        holder.binding.tvDay.text = dayFormatter.format(day).uppercase(Locale.ENGLISH)
        holder.binding.tvCondition.text = conditionFor(item.weatherCode)
        holder.binding.tvPop.text = "—"
        holder.binding.tvRange.text = "${item.maxTemp.toInt()}° / ${item.minTemp.toInt()}°"
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemForecastDailyRowBinding) : RecyclerView.ViewHolder(binding.root)
}
