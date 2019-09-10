package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*

class InvoiceService(private val dal: AntaeusDal) {
    fun create(
        service: String,
        amount: Money,
        customer: Customer,
        status: InvoiceStatus = InvoiceStatus.PENDING
    ): Invoice {
        return dal.createInvoice(service, amount, customer, status)!! /*TODO!!*/
    }

    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun updateStatus(invoice: Invoice, status: InvoiceStatus): Invoice {
        // TODO use this function somewhere!
        return dal.updateInvoiceStatus(invoice.id, status)
    }
}
