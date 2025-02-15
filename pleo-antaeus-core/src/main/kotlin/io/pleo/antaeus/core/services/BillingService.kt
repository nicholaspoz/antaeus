package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvalidJobTypeException
import io.pleo.antaeus.core.jobs.JobRunner
import io.pleo.antaeus.core.jobs.JobRunnerFactory
import io.pleo.antaeus.core.jobs.JobType
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus
import org.joda.time.DateTime
import kotlin.concurrent.thread

/**
 * The BillingService class is responsible for scheduling Billing jobs.
 * It is part of the JobRunner framework for running jobs in the background.
 */
class BillingService(
    private val cronJobService: CronJobService,
    private val jobRunnerFactory: JobRunnerFactory
) {

    @Synchronized
    @Throws(InvalidJobTypeException::class)
    fun runBillingJob(jobType: JobType, period: DateTime): Pair<CronJob, Thread?> {
        val jobRunner = jobRunnerFactory.getJobRunner(jobType, period)
        val job = cronJobService.getOrCreateByName(jobRunner.name)

        var thread: Thread? = null
        if (job.status == CronJobStatus.CREATED) {
            thread = runAsync(jobRunner)
        }
        return Pair(job, thread)
    }

    private fun runAsync(jobRunner: JobRunner): Thread {
        cronJobService.updateStatusByName(jobRunner.name, CronJobStatus.RUNNING)
        return thread {
            try {
                jobRunner.run()
            } finally {
                cronJobService.updateStatusByName(
                    jobRunner.name,
                    CronJobStatus.FINISHED
                )
            }
        }
    }
}
