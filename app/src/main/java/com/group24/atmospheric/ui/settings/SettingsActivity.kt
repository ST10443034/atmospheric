package com.group24.atmospheric.ui.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.group24.atmospheric.databinding.ActivitySettingsBinding
import com.group24.atmospheric.ui.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Settings Activity for app preferences.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        setupSpinners()
        observeState()
        
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleNotifications(isChecked)
        }
    }

    private fun setupSpinners() {
        val themes = arrayOf("Light", "Dark", "System")
        binding.spinnerTheme.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, themes)

        val units = arrayOf("Metric (°C)", "Imperial (°F)")
        binding.spinnerUnits.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, units)

        val languages = arrayOf("English", "isiZulu", "Afrikaans")
        binding.spinnerLanguage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.notificationsEnabled.collect { enabled ->
                binding.switchNotifications.isChecked = enabled
            }
        }
    }
}
