package com.group24.atmospheric.data.repository

import com.group24.atmospheric.data.local.ConnectivityObserver
import com.group24.atmospheric.data.local.SyncQueueEntity
import com.group24.atmospheric.data.local.WeatherDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository for the Sync & offline screen: connectivity state plus the pending
 * [SyncQueueEntity] rows queued while offline.
 */
class SyncRepository(
    private val dao: WeatherDao,
    private val connectivityObserver: ConnectivityObserver
) {
    val isOnline: Flow<Boolean> = connectivityObserver.isOnline

    val queue: Flow<List<SyncQueueEntity>> = dao.getQueueItems()

    suspend fun enqueue(operation: String, payload: String = "") {
        dao.addToQueue(
            SyncQueueEntity(
                operation = operation,
                payload = payload,
                status = "PENDING",
                retryCount = 0,
                createdAt = System.currentTimeMillis() / 1000
            )
        )
    }

    suspend fun markRetrying(item: SyncQueueEntity) {
        dao.updateQueueItem(item.copy(status = "RETRYING", retryCount = item.retryCount + 1))
    }

    suspend fun markSyncing(item: SyncQueueEntity) {
        dao.updateQueueItem(item.copy(status = "SYNCING"))
    }

    suspend fun complete(item: SyncQueueEntity) {
        dao.removeFromQueue(item.queueId)
    }
}
