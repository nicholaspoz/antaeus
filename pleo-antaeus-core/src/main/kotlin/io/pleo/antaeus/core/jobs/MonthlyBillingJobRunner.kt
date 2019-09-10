package io.pleo.antaeus.core.jobs

import io.pleo.antaeus.core.services.ChargeService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.Money
import org.joda.time.DateTime
import java.math.BigDecimal

class MonthlyBillingJobRunner(
    private val period: DateTime,
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService,
    private val chargeService: ChargeService
) : JobRunner {

    override fun getName(): String {
        val year = period.year.toString()
        val month = period.monthOfYear.toString().padStart(2,'0')
        return "${year}_$month"
    }

    override fun run() {
        customerService.fetchAll().forEach { customer ->
            val invoice = createInvoice(customer)
            val charge = chargeInvoice(customer, invoice)
            if (charge.status == ChargeStatus.FAILED) {
                customerService.notifyCustomerOfChargeFailure(charge)
            }
        }
    }

    private fun createInvoice(customer: Customer): Invoice {
        // TODO get the amount from somewhere
        val amount = Money(BigDecimal.valueOf(100.0), customer.currency)

        return invoiceService.create(
            service = getName(),
            customer = customer,
            amount = amount
        )
    }

    private fun chargeInvoice(customer: Customer, invoice: Invoice): Charge {
        return chargeService.create(
            customer = customer,
            invoice = invoice,
            amount = invoice.amount
        )
    }
}