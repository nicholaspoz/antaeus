package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus
import kotlin.concurrent.thread

class BillingService(
    private val cronJobService: CronJobService
) {
    @Synchronized
    fun runBillingJob(): CronJob {
        val name = "CRON_JOB"
        val job = cronJobService.getOrCreateByName(name)
        if (job.status == CronJobStatus.CREATED) runAsync(job)
        return job
    }

    private fun runAsync(job: CronJob) {
        cronJobService.updateStatusByName(job.name, CronJobStatus.RUNNING)
        thread {
            try {
                Thread.sleep(5000L)
            } finally {
                cronJobService.updateStatusByName(job.name, CronJobStatus.FINISHED)
            }
        }
    }
}
