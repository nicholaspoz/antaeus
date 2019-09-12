package io.pleo.antaeus.core.jobs

interface JobRunner : Runnable {
    val name: String
}
