/*
    Defines database tables and their schemas.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import org.jetbrains.exposed.sql.Table

object CustomerTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val currency = varchar("currency", 3)
}

object InvoiceTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val service = varchar("service", 50).index()
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val customerId = reference("customer_id", CustomerTable.id)
    val status = text("status")
}

object ChargeTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val invoiceId = reference("invoice_id", InvoiceTable.id)
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val status = text("status")
}

object CronJobTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 128).uniqueIndex()
    val status = varchar("status", 20)
    val started = datetime("started")
}