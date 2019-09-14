package io.pleo.antaeus.rest.models

import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus

data class BillingCronResponse(
    val id: Int,
    val name: String,
    val status: CronJobStatus,
    val started: String
)

fun CronJob.serialize() = BillingCronResponse(
    id = id,
    name = name,
    status = status,
    started = started.toString()
)

