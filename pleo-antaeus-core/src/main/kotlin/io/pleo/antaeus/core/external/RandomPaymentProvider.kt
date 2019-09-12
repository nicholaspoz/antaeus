package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Invoice
import kotlin.random.Random

class RandomPaymentProvider: PaymentProvider {
    override fun charge(invoice: Invoice): ChargeStatus {
        return if (Random.nextBoolean()) ChargeStatus.PAID else ChargeStatus.FAILED
    }
}
