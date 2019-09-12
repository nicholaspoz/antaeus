/*
    Defines mappings between database rows and Kotlin objects.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toInvoice(): Invoice = Invoice(
    id = this[InvoiceTable.id],
    item = this[InvoiceTable.item],
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

fun ResultRow.toCharge(): Charge = Charge(
    id = this[ChargeTable.id],
    amount = Money(
        value = this[ChargeTable.value],
        currency = Currency.valueOf(this[ChargeTable.currency])
    ),
    status = ChargeStatus.valueOf(this[ChargeTable.status]),
    invoiceId = this[ChargeTable.invoiceId]
)

fun ResultRow.toCronJob(): CronJob = CronJob(
    id = this[CronJobTable.id],
    name = this[CronJobTable.name],
    status = CronJobStatus.valueOf(this[CronJobTable.status]),
    started = this[CronJobTable.started]
)
