package com.group24.atmospheric.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.group24.atmospheric.databinding.ActivityDashboardBinding
import com.group24.atmospheric.ui.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Main dashboard Activity.
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        observeState()
        
        // Prototype: Load for a default location (e.g., Johannesburg)
        viewModel.loadWeather(-26.2041, 28.0473, 1L)
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is DashboardUiState.Loading -> {
                        // Show some loading indicator if needed
                    }
                    is DashboardUiState.Success -> {
                        updateUi(state.data)
                    }
                    is DashboardUiState.Error -> {
                        Toast.makeText(this@DashboardActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun updateUi(weather: com.group24.atmospheric.domain.model.WeatherInfo) {
        val current = weather.current
        binding.tvCurrentTemp.text = "${current.temperature}°C"
        binding.tvCondition.text = "Code: ${current.weatherCode}"
        binding.detailHumidity.tvDetailValue.text = "${current.humidity}%"
        binding.detailHumidity.tvDetailLabel.text = "Humidity"
        
        binding.detailWind.tvDetailValue.text = "${current.windSpeed} km/h"
        binding.detailWind.tvDetailLabel.text = "Wind"
        
        binding.detailPrecip.tvDetailValue.text = "${current.precipitation} mm"
        binding.detailPrecip.tvDetailLabel.text = "Precip"

        binding.tvLastUpdated.text = "Updated: ${java.time.Instant.ofEpochSecond(current.timestamp)}"
    }
}
