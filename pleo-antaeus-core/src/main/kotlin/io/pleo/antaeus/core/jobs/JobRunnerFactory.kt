package io.pleo.antaeus.core.jobs

import io.pleo.antaeus.core.exceptions.InvalidJobTypeException
import io.pleo.antaeus.core.services.ChargeService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import org.joda.time.DateTime

class JobRunnerFactory(
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService,
    private val chargeService: ChargeService
) {

    @Throws(InvalidJobTypeException::class)
    fun getJobRunner(jobType: JobType, period: DateTime): JobRunner = when (jobType) {
        JobType.MONTHLY_BILLING -> MonthlyBillingJobRunner(
            customerService = customerService,
            invoiceService = invoiceService,
            chargeService = chargeService,
            period = period
        )
        JobType.WEEKLY_SETTLEMENT -> WeeklySettlementJobRunner(
            customerService = customerService,
            invoiceService = invoiceService,
            chargeService = chargeService,
            period = period
        )
        else -> throw InvalidJobTypeException("Requested job is not implemented.")
    }

}
