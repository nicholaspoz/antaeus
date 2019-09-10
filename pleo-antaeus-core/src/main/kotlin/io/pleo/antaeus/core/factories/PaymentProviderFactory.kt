package io.pleo.antaeus.core.factories

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.external.RandomPaymentProvider
import io.pleo.antaeus.models.Customer

class PaymentProviderFactory(private val randomPaymentProvider: RandomPaymentProvider) {
    fun getPaymentProvider(customer: Customer): PaymentProvider {
        return randomPaymentProvider
    }
}