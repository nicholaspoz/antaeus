/*
    Implements the data access layer (DAL).
    This file implements the database queries used to fetch and insert rows in our database tables.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class AntaeusDal(private val db: Database) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun fetchPendingInvoicesAfter(cutoff: DateTime): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .select {
                    InvoiceTable.status.eq(InvoiceStatus.PENDING.toString()) and
                            InvoiceTable.added.greaterEq(cutoff)
                }
                .map { it.toInvoice() }
        }
    }

    fun createInvoice(
        item: String,
        amount: Money,
        customer: Customer,
        status: InvoiceStatus = InvoiceStatus.PENDING
    ): Invoice {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.item] = item
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                    it[this.added] = DateTime(DateTimeZone.UTC) // now
                } get InvoiceTable.id
        }

        return fetchInvoice(id!!)!!
    }

    fun updateInvoiceStatus(invoice: Invoice, status: InvoiceStatus): Invoice {
        transaction(db) {
            InvoiceTable
                .update({ InvoiceTable.id.eq(invoice.id) }) {
                    it[InvoiceTable.status] = status.toString()
                }
        }

        return fetchInvoice(invoice.id)!!
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id!!)!!
    }

    fun createChargeFromInvoice(invoice: Invoice, status: ChargeStatus): Charge {
        val id = transaction(db) {
            ChargeTable.insert {
                it[this.invoiceId] = invoice.id
                it[this.currency] = invoice.amount.currency.toString()
                it[this.value] = invoice.amount.value
                it[this.status] = status.toString()
                it[this.added] = DateTime(DateTimeZone.UTC) // now
            } get ChargeTable.id
        }

        return fetchCharge(id!!)!!
    }

    fun fetchCharge(id: Int): Charge? {
        return transaction(db) {
            ChargeTable
                .select { ChargeTable.id.eq(id) }
                .firstOrNull()
                ?.toCharge()
        }
    }

    fun getOrCreateCronJobByName(
        name: String,
        status: CronJobStatus = CronJobStatus.CREATED
    ): CronJob? {
        val existingJob = fetchCronJobByName(name)
        if (existingJob != null) {
            return existingJob
        }

        transaction(db) {
            CronJobTable
                .insert {
                    it[this.name] = name
                    it[this.status] = status.toString()
                    it[this.started] = DateTime(DateTimeZone.UTC) // now
                }
        }

        return fetchCronJobByName(name)
    }

    fun fetchCronJobByName(name: String): CronJob? {
        return transaction(db) {
            CronJobTable
                .select { CronJobTable.name.eq(name) }
                .firstOrNull()
                ?.toCronJob()
        }
    }

    fun updateCronJobStatusByName(name: String, status: CronJobStatus): CronJob? {
        transaction(db) {
            CronJobTable
                .update({ CronJobTable.name.eq(name) }) {
                    it[CronJobTable.status] = status.toString()
                }
        }

        return fetchCronJobByName(name)
    }
}
