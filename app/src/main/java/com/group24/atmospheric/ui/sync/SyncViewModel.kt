package com.group24.atmospheric.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group24.atmospheric.data.local.SyncQueueEntity
import com.group24.atmospheric.data.repository.SyncRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Sync & offline screen.
 * Role: surfaces connectivity + the pending [SyncQueueEntity] queue, and drives retry.
 */
class SyncViewModel(private val repository: SyncRepository) : ViewModel() {

    val uiState: StateFlow<SyncUiState> = combine(
        repository.isOnline,
        repository.queue
    ) { online, queue -> SyncUiState(isOnline = online, queue = queue) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncUiState())

    private val _snackbar = MutableSharedFlow<String>()
    val snackbar = _snackbar

    init {
        // Mirror the prototype: losing connectivity enqueues the standard offline-first batch.
        viewModelScope.launch {
            repository.isOnline.distinctUntilChanged().collect { online ->
                if (!online) {
                    val currentQueue = repository.queue.first()
                    if (currentQueue.isEmpty()) {
                        repository.enqueue("REFRESH_FORECAST")
                        repository.enqueue("UPDATE_PREFERENCES")
                        repository.enqueue("SYNC_LOCATIONS")
                    }
                } else {
                    val pending = repository.queue.first()
                    if (pending.isNotEmpty()) {
                        _snackbar.emit("Back online — WorkManager will sync shortly.")
                        runSync(pending)
                    }
                }
            }
        }
    }

    fun retryNow() {
        viewModelScope.launch {
            val state = uiState.value
            if (state.queue.isEmpty()) {
                _snackbar.emit("Nothing to sync.")
                return@launch
            }
            if (!state.isOnline) {
                state.queue.forEach { repository.markRetrying(it) }
                _snackbar.emit("No network — worker deferred with back-off.")
            } else {
                runSync(state.queue)
            }
        }
    }

    private suspend fun runSync(queue: List<SyncQueueEntity>) {
        queue.forEach { repository.markSyncing(it) }
        delay(900)
        queue.forEach { repository.complete(it) }
        _snackbar.emit("Sync complete. Forecast refreshed.")
    }
}

data class SyncUiState(
    val isOnline: Boolean = true,
    val queue: List<SyncQueueEntity> = emptyList()
)
