package io.pleo.antaeus.models

data class Invoice(
    val id: Int,
    val service: String,
    val customerId: Int,
    val amount: Money,
    val status: InvoiceStatus
)
