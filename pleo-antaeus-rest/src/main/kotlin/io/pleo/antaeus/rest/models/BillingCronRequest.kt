package io.pleo.antaeus.rest.models

import io.pleo.antaeus.core.jobs.JobType
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

data class BillingCronRequest(val jobType: String, val period: String)

fun BillingCronRequest.deserialize(): Pair<JobType, DateTime> {
    // TODO Javalin probably has a way to deserialize complex types
    val jt = JobType.valueOf(jobType)
    val pd = ISODateTimeFormat.dateTimeParser().parseDateTime(period)
    return Pair(jt, pd)
}
