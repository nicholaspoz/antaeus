package io.pleo.antaeus.models

data class Invoice(
    val id: Int,
    val item: String,
    val customerId: Int,
    val amount: Money,
    val status: InvoiceStatus
)
