package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.Customer

class PaymentProviderFactory(private val randomPaymentProvider: RandomPaymentProvider) {
    fun getPaymentProvider(customer: Customer): PaymentProvider {
        return randomPaymentProvider
    }
}
