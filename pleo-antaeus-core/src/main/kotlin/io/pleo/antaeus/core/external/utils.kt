package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import kotlin.random.Random

// This is the mocked instance of the payment provider
fun getPaymentProvider(customer: Customer): PaymentProvider {
    return object : PaymentProvider {
        override fun charge(invoice: Invoice): ChargeStatus {
            return if (Random.nextBoolean()) ChargeStatus.PAID else ChargeStatus.FAILED
        }
    }
}