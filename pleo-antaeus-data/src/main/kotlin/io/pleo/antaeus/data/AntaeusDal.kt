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

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    fun createInvoice(
        service: String,
        amount: Money,
        customer: Customer,
        status: InvoiceStatus = InvoiceStatus.PENDING
    ): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.service] = service
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                } get InvoiceTable.id
        }

        return fetchInvoice(id!!)
    }

    fun fetchPendingInvoicesByCustomerId(customerId: Int): List<Invoice> {
        val exp1 = InvoiceTable.customerId.eq(customerId)
        val exp2 = InvoiceTable.status.eq(InvoiceStatus.PENDING.toString())
        val query = exp1 and exp2
        return transaction(db) {
            InvoiceTable
                .select { query }
                .map { it.toInvoice() }
        }
    }

    fun updateInvoiceStatus(invoiceId: Int, status: InvoiceStatus): Invoice {
        val id = transaction(db) {
            InvoiceTable.update({ InvoiceTable.id.eq(invoiceId) }) {
                it[InvoiceTable.status] = status.toString()
            }
        }
        return fetchInvoice(id)!!
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

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id!!)
    }

    fun getOrCreateCronJob(
        name: String,
        status: CronJobStatus = CronJobStatus.CREATED
    ): CronJob {
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

        return fetchCronJobByName(name)!!
    }

    fun fetchCronJobByName(name: String): CronJob? {
        return transaction(db) {
            CronJobTable
                .select { CronJobTable.name.eq(name) }
                .firstOrNull()
                ?.toCronJob()
        }
    }

    fun updateCronJobStatusByName(name: String, status: CronJobStatus): CronJob {
        transaction(db) {
            CronJobTable
                .update({ CronJobTable.name.eq(name) }) {
                    it[CronJobTable.status] = status.toString()
                }
        }

        return fetchCronJobByName(name)!!
    }

    fun createCharge(invoice: Invoice, amount: Money, status: ChargeStatus): Charge {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            ChargeTable.insert {
                it[this.invoiceId] = invoice.id
                it[this.currency] = amount.currency.toString()
                it[this.value] = amount.value
                it[this.status] = status.toString()
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
}
