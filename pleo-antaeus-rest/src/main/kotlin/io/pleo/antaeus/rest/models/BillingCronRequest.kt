package io.pleo.antaeus.rest.models

import io.pleo.antaeus.core.jobs.JobType
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

data class BillingCronRequest(val rawJobType: String, val rawPeriod: String)

fun BillingCronRequest.deserialize(): Pair<JobType, DateTime> {
    // TODO Javalin probably has a way to deserialize complex types
    val jobType = JobType.valueOf(rawJobType)
    val period = ISODateTimeFormat.dateTimeParser().parseDateTime(rawPeriod)
    return Pair(jobType, period)
}
