package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.getPaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.Money

class ChargeService(private val dal: AntaeusDal) {

    fun create(
        customer: Customer,
        invoice: Invoice,
        amount: Money
    ): Charge {
        val paymentProvider = getPaymentProvider(customer)
        val status = paymentProvider.charge(invoice)
        return dal.createCharge(invoice, amount, status)
    }
}