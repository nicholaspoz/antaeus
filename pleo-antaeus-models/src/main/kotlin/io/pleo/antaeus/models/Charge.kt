package io.pleo.antaeus.models

import org.joda.time.DateTime

data class Charge(
    val id: Int,
    val invoiceId: Int,
    val amount: Money,
    val status: ChargeStatus,
    val added: DateTime
)
