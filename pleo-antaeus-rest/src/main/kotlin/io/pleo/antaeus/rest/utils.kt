package io.pleo.antaeus.rest

import io.pleo.antaeus.core.jobs.JobType
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

data class BillingCronRequest(val cron: String, val period: String)

fun deserializeCronRequest(rawCron: String, rawPeriod: String): Pair<JobType, DateTime> {
    // TODO Javalin probably has a way to deserialize complex types
    val jobType = JobType.valueOf(rawCron)
    val period = ISODateTimeFormat.dateTimeParser().parseDateTime(rawPeriod)
    return Pair(jobType, period)
}