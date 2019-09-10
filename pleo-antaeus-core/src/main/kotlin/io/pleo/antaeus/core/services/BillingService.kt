package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.factories.BillingJobFactory
import io.pleo.antaeus.core.jobs.JobRunner
import io.pleo.antaeus.core.jobs.JobType
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus
import org.joda.time.DateTime
import kotlin.concurrent.thread

class BillingService(
    private val billingJobFactory: BillingJobFactory,
    private val cronJobService: CronJobService
) {

    @Synchronized
    fun startJob(jobType: JobType, period: DateTime): Pair<CronJob, Thread?> {
        val jobRunner = billingJobFactory.getJobRunner(jobType, period)
        val job = cronJobService.getOrCreateCronJob(jobRunner.getName())

        var t: Thread? = null
        if (job.status == CronJobStatus.CREATED) {
            t = runJob(jobRunner)
        }
        return Pair(job, t)
    }

    private fun runJob(jobRunner: JobRunner): Thread {
        return thread {
            cronJobService.updateStatusByName(jobRunner.getName(), CronJobStatus.RUNNING)
            try {
                jobRunner.process()
            } finally {
                cronJobService.updateStatusByName(jobRunner.getName(), CronJobStatus.FINISHED)
            }
        }
    }

}
