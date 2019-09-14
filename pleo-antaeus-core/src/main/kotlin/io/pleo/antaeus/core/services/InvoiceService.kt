/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun create(
        item: String,
        amount: Money,
        customer: Customer,
        status: InvoiceStatus = InvoiceStatus.PENDING
    ): Invoice {
        return dal.createInvoice(item, amount, customer, status)
    }

    fun updateStatus(invoice: Invoice, status: InvoiceStatus): Invoice {
        return dal.updateInvoiceStatus(invoice, status)
    }
}
