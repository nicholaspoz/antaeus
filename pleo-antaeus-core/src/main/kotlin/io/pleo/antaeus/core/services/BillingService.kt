package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService
) {
    fun chargeInvoices() {
        customerService.fetchAll().forEach { customer ->
            invoiceService.fetchPendingForCustomer(customer.id).forEach { invoice ->
                val charged = paymentProvider.charge(invoice)
                val status = if(charged) InvoiceStatus.PAID else InvoiceStatus.PENDING
                invoiceService.updateStatus(invoice, status)
                println("Updated invoice $invoice with status $status")
            }
        }
    }
}