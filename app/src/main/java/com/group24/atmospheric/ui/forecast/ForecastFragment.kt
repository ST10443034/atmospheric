package com.group24.atmospheric.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.group24.atmospheric.R
import com.group24.atmospheric.databinding.FragmentForecastBinding
import com.group24.atmospheric.domain.model.WeatherInfo
import com.group24.atmospheric.ui.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Forecast & graph: hourly list, daily list, and a single-series graph of a chosen variable.
 */
class ForecastFragment : Fragment() {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ForecastViewModel
    private val hourlyAdapter = HourlyRowAdapter()
    private val dailyAdapter = DailyRowAdapter()

    // Matches Dashboard's fixed repo-default location.
    private val lat = -26.2041
    private val lon = 28.0473
    private val locationId = 1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[ForecastViewModel::class.java]

        binding.rvHourly.adapter = hourlyAdapter
        binding.rvDaily.adapter = dailyAdapter

        binding.tabHourly.setOnClickListener { viewModel.selectTab(ForecastTab.HOURLY) }
        binding.tabDaily.setOnClickListener { viewModel.selectTab(ForecastTab.DAILY) }
        binding.tabGraph.setOnClickListener { viewModel.selectTab(ForecastTab.GRAPH) }

        binding.chipTemperature.setOnClickListener { viewModel.selectVariable(GraphVariable.TEMPERATURE) }
        binding.chipPrecip.setOnClickListener { viewModel.selectVariable(GraphVariable.PRECIPITATION) }
        binding.chipHumidity.setOnClickListener { viewModel.selectVariable(GraphVariable.HUMIDITY) }
        binding.chipWind.setOnClickListener { viewModel.selectVariable(GraphVariable.WIND) }

        observeState()
        viewModel.loadWeather(lat, lon, locationId)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                renderTabs(state.selectedTab)
                renderChips(state.selectedVariable)
                state.weather?.let { renderContent(it, state.selectedTab, state.selectedVariable) }
            }
        }
    }

    private fun renderTabs(selected: ForecastTab) {
        binding.rvHourly.visibility = if (selected == ForecastTab.HOURLY) View.VISIBLE else View.GONE
        binding.rvDaily.visibility = if (selected == ForecastTab.DAILY) View.VISIBLE else View.GONE
        binding.graphContainer.visibility = if (selected == ForecastTab.GRAPH) View.VISIBLE else View.GONE

        setTabSelected(binding.tabHourly, selected == ForecastTab.HOURLY)
        setTabSelected(binding.tabDaily, selected == ForecastTab.DAILY)
        setTabSelected(binding.tabGraph, selected == ForecastTab.GRAPH)
    }

    private fun setTabSelected(tab: TextView, isSelected: Boolean) {
        tab.setBackgroundResource(if (isSelected) R.drawable.bg_segment_selected else 0)
        tab.setTextColor(requireContext().getColor(if (isSelected) R.color.ink else R.color.ink_muted))
    }

    private fun renderChips(selected: GraphVariable) {
        setChipSelected(binding.chipTemperature, selected == GraphVariable.TEMPERATURE)
        setChipSelected(binding.chipPrecip, selected == GraphVariable.PRECIPITATION)
        setChipSelected(binding.chipHumidity, selected == GraphVariable.HUMIDITY)
        setChipSelected(binding.chipWind, selected == GraphVariable.WIND)
    }

    private fun setChipSelected(chip: TextView, isSelected: Boolean) {
        chip.setBackgroundResource(if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        chip.setTextColor(requireContext().getColor(if (isSelected) R.color.link else R.color.ink))
    }

    private fun renderContent(weather: WeatherInfo, tab: ForecastTab, variable: GraphVariable) {
        hourlyAdapter.submitList(weather.hourly)
        dailyAdapter.submitList(weather.daily)

        if (tab == ForecastTab.GRAPH) {
            binding.tvGraphHeader.text = "${variable.apiName} · 24H"
            binding.tvGraphFreshness.text = if (weather.isCached) "CACHED" else "LIVE"
            binding.tvGraphFreshness.setTextColor(
                requireContext().getColor(if (weather.isCached) R.color.warn else R.color.link)
            )

            val values = when (variable) {
                GraphVariable.TEMPERATURE -> weather.hourly.map { it.temperature.toFloat() }
                GraphVariable.PRECIPITATION -> weather.hourly.map { it.precipitationProbability.toFloat() }
                GraphVariable.HUMIDITY -> weather.hourly.map { weather.current.humidity.toFloat() }
                GraphVariable.WIND -> weather.hourly.map { weather.current.windSpeed.toFloat() }
            }
            binding.graphView.setData(values, weather.isCached)

            if (values.isNotEmpty()) {
                val min = values.min()
                val max = values.max()
                binding.tvGraphValue.text = "${values.first().toInt()}°"
                binding.tvGraphRange.text = "now · ${min.toInt()}° – ${max.toInt()}°"
            }
        }
        binding.tvRequestShape.text =
            "GET /v1/forecast?latitude=$lat&longitude=$lon&timezone=auto"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
