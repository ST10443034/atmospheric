package com.group24.atmospheric.ui.sync

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.group24.atmospheric.R
import com.group24.atmospheric.databinding.FragmentSyncBinding
import com.group24.atmospheric.ui.ViewModelFactory
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Sync & offline: connectivity status plus the pending SyncQueue, per the offline-first strategy.
 */
class SyncFragment : Fragment() {

    private var _binding: FragmentSyncBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SyncViewModel
    private val queueAdapter = SyncQueueAdapter()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[SyncViewModel::class.java]

        binding.rvQueue.adapter = queueAdapter
        binding.btnRetryNow.setOnClickListener { viewModel.retryNow() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state -> render(state) }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.snackbar.collect { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun render(state: SyncUiState) {
        val context = requireContext()
        val now = timeFormatter.format(Instant.now().atZone(ZoneId.systemDefault()))

        if (state.isOnline) {
            binding.statusCard.setBackgroundResource(R.drawable.bg_card_status_online)
            binding.dotStatus.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.link))
            binding.tvStatusName.text = getString(R.string.sync_status_online)
            binding.tvStatusName.setTextColor(context.getColor(R.color.link))
            binding.tvStatusBlurb.text = getString(R.string.sync_blurb_online)
        } else {
            binding.statusCard.setBackgroundResource(R.drawable.bg_card_status_offline)
            binding.dotStatus.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.warn))
            binding.tvStatusName.text = getString(R.string.sync_status_offline)
            binding.tvStatusName.setTextColor(context.getColor(R.color.warn))
            binding.tvStatusBlurb.text = getString(R.string.sync_blurb_offline)
        }

        val pending = state.queue.count { it.status == "PENDING" || it.status == "RETRYING" }
        val retries = state.queue.sumOf { it.retryCount }
        binding.tvStatusStats.text = "CACHED $now · PENDING $pending · RETRIES $retries"

        if (state.queue.isEmpty()) {
            binding.rvQueue.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.rvQueue.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
            queueAdapter.submitList(state.queue)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
