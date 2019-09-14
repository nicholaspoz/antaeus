package io.pleo.antaeus.core.jobs

import org.joda.time.DateTime

interface JobRunner : Runnable {
    val name: String
    val period: DateTime
}
