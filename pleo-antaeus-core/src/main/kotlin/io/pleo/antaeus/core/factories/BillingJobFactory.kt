package io.pleo.antaeus.core.factories

import io.pleo.antaeus.core.jobs.JobRunner
import io.pleo.antaeus.core.jobs.JobType
import io.pleo.antaeus.core.jobs.MonthlyBillingJobRunner
import io.pleo.antaeus.core.services.ChargeService
import io.pleo.antaeus.core.services.CronJobService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import org.joda.time.DateTime
import java.lang.RuntimeException

class BillingJobFactory(
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService,
    private val chargeService: ChargeService,
    private val cronJobService: CronJobService
) {

    fun getJobRunner(jobType: JobType, period: DateTime): JobRunner = when (jobType) {
        JobType.MONTHLY_BILLING -> MonthlyBillingJobRunner(
            period,
            customerService,
            invoiceService,
            chargeService,
            cronJobService
        )
        else -> throw RuntimeException("Oops") // TODO
    }
}