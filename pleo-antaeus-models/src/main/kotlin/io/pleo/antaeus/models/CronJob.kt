package io.pleo.antaeus.models
import org.joda.time.DateTime

data class CronJob(
    val id: Int,
    val name: String,
    val status: CronJobStatus,
    val started: DateTime
)
