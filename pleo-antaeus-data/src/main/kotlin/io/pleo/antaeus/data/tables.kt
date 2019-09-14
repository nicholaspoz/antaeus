/*
    Defines database tables and their schemas.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import org.jetbrains.exposed.sql.Table

object InvoiceTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val item = varchar("item", 50)
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val customerId = reference("customer_id", CustomerTable.id)
    val status = text("status")
    val added = datetime("added")
}

object ChargeTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val invoiceId = reference("invoice_id", InvoiceTable.id)
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val status = text("status")
    val added = datetime("added")
}

object CustomerTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val currency = varchar("currency", 3)
}

object CronJobTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 128).uniqueIndex()
    val status = varchar("status", 20)
    val started = datetime("started")
}
