/*
    Defines mappings between database rows and Kotlin objects.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toInvoice(): Invoice = Invoice(
    id = this[InvoiceTable.id],
    service = this[InvoiceTable.service],
    amount = Money(
        value = this[InvoiceTable.value],
        currency = Currency.valueOf(this[InvoiceTable.currency])
    ),
    status = InvoiceStatus.valueOf(this[InvoiceTable.status]),
    customerId = this[InvoiceTable.customerId]
)

fun ResultRow.toCustomer(): Customer = Customer(
    id = this[CustomerTable.id],
    currency = Currency.valueOf(this[CustomerTable.currency])
)

fun ResultRow.toCronJob(): CronJob = CronJob(
    id = this[CronJobTable.id],
    name = this[CronJobTable.name],
    status = CronJobStatus.valueOf(this[CronJobTable.status]),
    started = this[CronJobTable.started]
)

fun ResultRow.toCharge(): Charge = Charge(
    id = this[ChargeTable.id],
    amount = Money(
        value = this[ChargeTable.value],
        currency = Currency.valueOf(this[ChargeTable.currency])
    ),
    status = ChargeStatus.valueOf(this[ChargeTable.status]),
    invoiceId = this[ChargeTable.invoiceId]
)
