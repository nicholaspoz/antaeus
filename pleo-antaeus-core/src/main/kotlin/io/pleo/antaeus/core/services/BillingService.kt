package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.factories.BillingJobFactory
import io.pleo.antaeus.core.jobs.JobRunner
import io.pleo.antaeus.core.jobs.JobType
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus
import org.joda.time.DateTime

class BillingService(
    private val billingJobFactory: BillingJobFactory,
    private val cronJobService: CronJobService
) {

    @Synchronized
    fun scheduleBillingJob(jobType: JobType, period: DateTime): CronJob {
        val jobRunner = billingJobFactory.getJobRunner(jobType, period)
        val name = jobRunner.getName()
        val job = cronJobService.getOrCreateCronJob(name)
        if (job.status == CronJobStatus.CREATED) {
            startJob(jobRunner)
        }
        return job
    }

    private fun startJob(jobRunner: JobRunner) {
        val t = Thread(jobRunner)
        t.start()
    }

}
