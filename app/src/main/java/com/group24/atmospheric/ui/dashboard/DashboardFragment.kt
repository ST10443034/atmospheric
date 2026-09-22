package com.group24.atmospheric.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group24.atmospheric.R
import com.group24.atmospheric.databinding.FragmentDashboardBinding
import com.group24.atmospheric.domain.model.WeatherInfo
import com.group24.atmospheric.ui.ViewModelFactory
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Dashboard: current conditions for the saved location, with a LIVE/CACHED freshness signal.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private val hourlyAdapter = HourlyForecastAdapter(emptyList())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Repo default location, matching the previous prototype's fixed location.
    private val locationName = "Johannesburg"
    private val lat = -26.2041
    private val lon = 28.0473
    private val locationId = 1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        binding.tvLocationName.text = locationName
        binding.tvCoordinates.text = "$lat, $lon"
        binding.rvHourlyForecast.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHourlyForecast.adapter = hourlyAdapter

        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.forecastFragment)
        }
        binding.btnRetry.setOnClickListener {
            viewModel.loadWeather(lat, lon, locationId)
        }

        observeState()
        viewModel.loadWeather(lat, lon, locationId)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is DashboardUiState.Loading -> showLoading()
                    is DashboardUiState.Success -> showSuccess(state.data)
                    is DashboardUiState.Error -> showError()
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvLoadingCaption.visibility = View.VISIBLE
        binding.groupSuccess.visibility = View.GONE
        binding.groupError.visibility = View.GONE
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        binding.tvLoadingCaption.visibility = View.GONE
        binding.groupSuccess.visibility = View.GONE
        binding.groupError.visibility = View.VISIBLE
    }

    private fun showSuccess(weather: WeatherInfo) {
        binding.progressBar.visibility = View.GONE
        binding.tvLoadingCaption.visibility = View.GONE
        binding.groupError.visibility = View.GONE
        binding.groupSuccess.visibility = View.VISIBLE

        val current = weather.current
        binding.tvCurrentTemp.text = current.temperature.toInt().toString()
        binding.tvUnit.text = "°C"
        binding.tvCondition.text = conditionFor(current.weatherCode)

        val dailyToday = weather.daily.firstOrNull()
        binding.tvFeelsLike.text = if (dailyToday != null) {
            "Feels like ${current.feelsLike.toInt()}° · ${dailyToday.maxTemp.toInt()}° / ${dailyToday.minTemp.toInt()}°"
        } else {
            "Feels like ${current.feelsLike.toInt()}°"
        }

        val time = Instant.ofEpochSecond(if (weather.isCached) weather.fetchedAt else current.timestamp)
            .atZone(ZoneId.systemDefault())
        if (weather.isCached) {
            binding.tvFreshness.text = "CACHED ${timeFormatter.format(time)} · OFFLINE SINCE ${timeFormatter.format(time)}"
            binding.tvFreshness.setTextColor(requireContext().getColor(R.color.warn))
            binding.pillStatus.setBackgroundResource(R.drawable.bg_pill_offline)
            binding.tvStatusPill.text = "CACHED"
            binding.tvStatusPill.setTextColor(requireContext().getColor(R.color.warn))
            binding.dotStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.warn))
        } else {
            binding.tvFreshness.text = "LAST UPDATED ${timeFormatter.format(time)} · LIVE"
            binding.tvFreshness.setTextColor(requireContext().getColor(R.color.link))
            binding.pillStatus.setBackgroundResource(R.drawable.bg_pill_online)
            binding.tvStatusPill.text = "LIVE"
            binding.tvStatusPill.setTextColor(requireContext().getColor(R.color.link))
            binding.dotStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.link))
        }

        binding.detailHumidity.tvDetailLabel.text = "HUMIDITY"
        binding.detailHumidity.tvDetailValue.text = "${current.humidity.toInt()}%"
        binding.detailWind.tvDetailLabel.text = "WIND"
        binding.detailWind.tvDetailValue.text = "${current.windSpeed.toInt()} km/h"
        binding.detailPrecip.tvDetailLabel.text = "PRECIP"
        binding.detailPrecip.tvDetailValue.text = "${current.precipitation.toInt()}%"

        hourlyAdapter.submitList(weather.hourly.take(5))
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
