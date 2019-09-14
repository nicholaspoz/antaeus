package io.pleo.antaeus.core.jobs

import io.pleo.antaeus.core.services.ChargeService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import org.joda.time.DateTime

class JobRunnerFactory(
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService,
    private val chargeService: ChargeService
) {
    fun getJobRunner(jobType: JobType, period: DateTime): JobRunner =
        throw NotImplementedError("TODO")
}
