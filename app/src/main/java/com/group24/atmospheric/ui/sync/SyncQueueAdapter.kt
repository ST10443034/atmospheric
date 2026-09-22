package com.group24.atmospheric.ui.sync

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.group24.atmospheric.R
import com.group24.atmospheric.data.local.SyncQueueEntity
import com.group24.atmospheric.databinding.ItemSyncQueueBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SyncQueueAdapter(private var items: List<SyncQueueEntity> = emptyList()) :
    RecyclerView.Adapter<SyncQueueAdapter.ViewHolder>() {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun submitList(newItems: List<SyncQueueEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSyncQueueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding
        binding.tvOperation.text = item.operation
        binding.tvStatus.text = item.status
        val time = Instant.ofEpochSecond(item.createdAt).atZone(ZoneId.systemDefault())
        binding.tvMeta.text = "ID %04d · %s · RETRY %d".format(item.queueId, timeFormatter.format(time), item.retryCount)

        val context = binding.root.context
        if (item.status == "SYNCING") {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_pill_online)
            binding.tvStatus.setTextColor(context.getColor(R.color.link))
        } else {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_pill_offline)
            binding.tvStatus.setTextColor(context.getColor(R.color.warn))
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemSyncQueueBinding) : RecyclerView.ViewHolder(binding.root)
}
