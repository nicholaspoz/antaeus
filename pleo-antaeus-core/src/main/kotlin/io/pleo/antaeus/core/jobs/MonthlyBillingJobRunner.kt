package io.pleo.antaeus.core.jobs

import io.pleo.antaeus.core.getSubscriptionAmount
import io.pleo.antaeus.core.services.ChargeService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.joda.time.DateTime

/**
 * The MonthlyBillingJobRunner is responsible for billing for customers' monthly
 * subscriptions. This is done by creating and charging invoices on the first of the
 * month.
 */
class MonthlyBillingJobRunner(
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService,
    private val chargeService: ChargeService,
    override val period: DateTime
) : JobRunner {
    override val name: String
        get() {
            val year = period.year.toString()
            val month = period.monthOfYear.toString().padStart(2, '0')
            return "${JobType.MONTHLY_BILLING}_${year}_$month"
        }

    override fun run() {
        customerService.fetchAll().forEach { customer ->
            val invoice = createInvoice(customer)
            val charge = chargeInvoice(customer, invoice)
            if (charge.status == ChargeStatus.PAID) {
                completeInvoice(invoice)
            } else {
                notifyCustomer(customer, charge)
            }
        }
    }

    private fun createInvoice(customer: Customer): Invoice {
        val amount = getSubscriptionAmount(customer.currency)
        return invoiceService.create(
            item = name,
            customer = customer,
            amount = amount
        )
    }

    private fun chargeInvoice(customer: Customer, invoice: Invoice): Charge {
        return chargeService.createFromInvoice(
            customer = customer,
            invoice = invoice
        )
    }

    private fun completeInvoice(invoice: Invoice) {
        invoiceService.updateStatus(invoice, InvoiceStatus.PAID)
    }

    private fun notifyCustomer(customer: Customer, charge: Charge) {
        customerService.notifyOfChargeFailure(customer, charge)
    }
}
