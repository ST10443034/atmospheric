package com.group24.atmospheric

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.group24.atmospheric.databinding.ActivityMainBinding
import com.group24.atmospheric.ui.ViewModelFactory
import com.group24.atmospheric.ui.sync.SyncViewModel
import kotlinx.coroutines.launch

/**
 * Single-Activity host: a [BottomNavigationView] driving the Dashboard, Forecast,
 * Sync/Offline and Settings fragments (everything after login).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.bottomNav.setupWithNavController(navHostFragment.navController)

        observeSyncBadge()
    }

    /** Turns the Offline tab amber with a pending-count badge, per the design's bottom-nav spec. */
    private fun observeSyncBadge() {
        val factory = ViewModelFactory(this)
        val syncViewModel = androidx.lifecycle.ViewModelProvider(this, factory)[SyncViewModel::class.java]
        lifecycleScope.launch {
            syncViewModel.uiState.collect { state ->
                val badge = binding.bottomNav.getOrCreateBadge(R.id.syncFragment)
                if (state.queue.isNotEmpty()) {
                    badge.isVisible = true
                    badge.number = state.queue.size
                    badge.backgroundColor = getColor(R.color.warn)
                    badge.badgeTextColor = getColor(R.color.bg)
                } else {
                    badge.isVisible = false
                }
            }
        }
    }
}
