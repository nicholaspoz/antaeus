/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import io.pleo.antaeus.core.external.PaymentProviderFactory
import io.pleo.antaeus.core.external.RandomPaymentProvider
import io.pleo.antaeus.core.jobs.JobRunnerFactory
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.ChargeService
import io.pleo.antaeus.core.services.CronJobService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.ChargeTable
import io.pleo.antaeus.data.CronJobTable
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.rest.AntaeusRest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import setupInitialData
import java.sql.Connection

fun main() {
    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable, ChargeTable, CronJobTable)

    // Connect to the database and create the needed tables. Drop any existing data.
    val db = Database
        .connect("jdbc:sqlite:/tmp/data.db", "org.sqlite.JDBC")
        .also {
            TransactionManager.manager.defaultIsolationLevel =
                Connection.TRANSACTION_SERIALIZABLE
            transaction(it) {
                addLogger(StdOutSqlLogger)
                // Drop all existing tables to ensure a clean slate on each run
                SchemaUtils.drop(*tables)
                // Create all tables
                SchemaUtils.create(*tables)
            }
        }

    // Set up data access layer.
    val dal = AntaeusDal(db = db)

    // Insert example data in the database.
    setupInitialData(dal = dal)

    // Get third parties
    val randomPaymentProvider = RandomPaymentProvider()
    val paymentProviderFactory = PaymentProviderFactory(
        randomPaymentProvider = randomPaymentProvider
    )

    // Create core services
    val invoiceService = InvoiceService(dal = dal)
    val customerService = CustomerService(dal = dal)
    val cronJobService = CronJobService(dal = dal)
    val chargeService = ChargeService(
        dal = dal,
        paymentProviderFactory = paymentProviderFactory
    )
    val jobRunnerFactory = JobRunnerFactory(
        customerService = customerService,
        invoiceService = invoiceService,
        chargeService = chargeService
    )

    // This is _your_ billing service to be included where you see fit
    val billingService = BillingService(cronJobService, jobRunnerFactory)

    // Create REST web service
    AntaeusRest(
        invoiceService = invoiceService,
        customerService = customerService,
        billingService = billingService
    ).run()
}

