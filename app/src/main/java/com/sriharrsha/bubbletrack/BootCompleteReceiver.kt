package com.sriharrsha.bubbletrack

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        context?.let {
            WorkManager.getInstance(it).enqueueUniquePeriodicWork(
                Constants.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                Constants.PERIODIC_WORK_REQUEST
            )
        }
    }

    companion object {
        private val TAG = BootCompleteReceiver::class.java.simpleName
    }
}