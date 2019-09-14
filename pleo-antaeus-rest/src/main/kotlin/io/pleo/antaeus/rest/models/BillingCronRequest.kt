package io.pleo.antaeus.rest.models

import io.pleo.antaeus.core.exceptions.InvalidJobTypeException
import io.pleo.antaeus.core.jobs.JobType
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.lang.IllegalArgumentException

data class BillingCronRequest(val jobType: String, val period: String)

fun BillingCronRequest.deserialize(): Pair<JobType, DateTime> {
    // TODO Javalin probably has a way to deserialize complex types
    val jt: JobType
    try {
        jt = JobType.valueOf(jobType)
    } catch (e: IllegalArgumentException) {
        throw InvalidJobTypeException("Invalid JobType provided.")
    }
    val pd = ISODateTimeFormat.dateTimeParser().parseDateTime(period)
    return Pair(jt, pd)
}
