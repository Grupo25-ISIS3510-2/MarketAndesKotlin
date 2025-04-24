package com.uniandes.marketandes.utils

import android.content.Context
import androidx.work.*
import com.uniandes.marketandes.worker.SyncMessagesWorker
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    fun enqueueSyncOnNetworkAvailable(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Solo si hay red
            .build()

        val request = OneTimeWorkRequestBuilder<SyncMessagesWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "syncMessagesOnConnection",
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}