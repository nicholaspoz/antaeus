package io.pleo.antaeus.models

data class Charge(
    val id: Int,
    val invoiceId: Int,
    val amount: Money,
    val status: ChargeStatus
)