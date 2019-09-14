package io.pleo.antaeus.core.jobs

import org.joda.time.DateTime

/**
 * Implementations of this class should define business logic for a billing job.
 * It is used by the BillingService to run long jobs asynchronously.
 *
 * The `name` param is used by the BillingService to uniquely identify a job. The
 * implementing class should decide if `name` and `period` are constructed or derived
 * from other fields.
 */
interface JobRunner : Runnable {
    val name: String
    val period: DateTime
}
