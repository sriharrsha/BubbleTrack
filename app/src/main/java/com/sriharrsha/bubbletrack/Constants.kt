package com.sriharrsha.bubbletrack

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import java.time.Duration

class Constants {
        companion object {
            val REPEAT_INTERVAL: Duration = Duration.ofMinutes(15)
            val MY_CONSTRAINTS: Constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()

            val PERIODIC_WORK_REQUEST: PeriodicWorkRequest =
                PeriodicWorkRequest.Builder(BackgroundScraper::class.java, REPEAT_INTERVAL)
                    .setConstraints(MY_CONSTRAINTS)
                    .build()
            val UNIQUE_WORK_NAME: String = "RHINO"

            val ONE_WORK_REQUEST: OneTimeWorkRequest = OneTimeWorkRequest.Builder(BackgroundScraper::class.java).build()
        }
}