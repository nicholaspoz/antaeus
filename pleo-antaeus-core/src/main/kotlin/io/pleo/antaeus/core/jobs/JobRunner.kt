package io.pleo.antaeus.core.jobs

import io.pleo.antaeus.core.services.CronJobService
import io.pleo.antaeus.models.CronJobStatus

abstract class JobRunner(
    private val cronJobService: CronJobService
) : Runnable {
    abstract fun getName(): String
    abstract fun process()

    override fun run() {
        cronJobService.updateStatusByName(getName(), CronJobStatus.RUNNING)
        try {
            process()
        } finally {
            cronJobService.updateStatusByName(getName(), CronJobStatus.FINISHED)
        }
    }
}