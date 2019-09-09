package io.pleo.antaeus.core.services

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus


class MonthlyBillingRunner(
    private val job: CronJob,
    private val billingService: BillingService,
    private val schedulingService: JobSchedulingService
    ) : Runnable {

    companion object {
        fun constructJobName(month: Int, year: Int): String {
            return "$year-$month"
        }

        fun spawn(
            job: CronJob,
            billingService: BillingService,
            jobSchedulingService: JobSchedulingService
        ) {
            val runner = MonthlyBillingRunner(job, billingService, jobSchedulingService)
            val t = Thread(runner)
            t.start()
        }
    }

    override fun run() {
        schedulingService.updateJobStatus(job, CronJobStatus.RUNNING)

        try {
            billingService.chargeInvoices()
        } finally {
            schedulingService.updateJobStatus(job, CronJobStatus.FINISHED)
        }
    }
}


class JobSchedulingService(
    private val billingService: BillingService,
    private val dal: AntaeusDal
) {

    @Synchronized fun scheduleMonthlyBilling(month: Int, year: Int): CronJob {
        val jobName = MonthlyBillingRunner.constructJobName(month, year)
        val job = dal.getOrCreateCronJob(
            name = jobName,
            type = "monthly-billing"
        )
        if (job.status == CronJobStatus.CREATED) {
            // Kick off the new job
            MonthlyBillingRunner.spawn(job, billingService, this)
        }

        return job
    }

    fun updateJobStatus(job: CronJob, status: CronJobStatus): CronJob {
        if (job.status == status) {
            return job
        }
        return dal.updateCronJobStatus(job, status)

    }
}