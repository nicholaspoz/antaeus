package io.pleo.antaeus.core.jobs

import io.pleo.antaeus.core.services.ChargeService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * The WeeklySettlmentJobRunner is responsible for ensuring payment on invoices that have
 * not been settled. It should run once a week. If an invoice can not be settled by the
 * end of the month, the Invoice will be marked as FAILED.
 */
class WeeklySettlementJobRunner(
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService,
    private val chargeService: ChargeService,
    override val period: DateTime
) : JobRunner {
    override val name: String
        get() {
            val year = period.year.toString()
            val week = period.weekOfWeekyear.toString().padStart(2, '0')
            return "${JobType.WEEKLY_SETTLEMENT}_${year}_${week}"
        }

    private val isLastWeekOfMonth: Boolean
        get() {
            val weekFromNow = period.plusDays(7)
            return period.monthOfYear != weekFromNow.monthOfYear
        }

    override fun run() {
        fetchPendingInvoices().forEach {invoice ->
            val customer = customerService.fetch(invoice.customerId)
            val charge = chargeInvoice(customer, invoice)
            if (charge.status == ChargeStatus.PAID) {
                completeInvoice(invoice)
            } else {
                handleChargeFailure(customer, invoice, charge)
            }
        }
    }

    private fun fetchPendingInvoices(): List<Invoice> {
        val monthStart = DateTime(
            period.year,
            period.monthOfYear,
            1,
            0,
            0,
            DateTimeZone.UTC
        )
        return invoiceService.fetchPendingAfter(cutoff = monthStart)
    }

    private fun chargeInvoice(customer: Customer, invoice: Invoice): Charge {
        return chargeService.createFromInvoice(customer, invoice)
    }

    private fun completeInvoice(invoice: Invoice) {
        invoiceService.updateStatus(invoice, InvoiceStatus.PAID)
    }

    private fun handleChargeFailure(
        customer: Customer,
        invoice: Invoice,
        charge: Charge
    ) {
        customerService.notifyOfChargeFailure(customer, charge)
        if (isLastWeekOfMonth) {
            invoiceService.updateStatus(invoice, InvoiceStatus.FAILED)
        }
    }
}
