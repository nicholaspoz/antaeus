package io.pleo.antaeus.core.jobs

interface JobRunner : Runnable {
    fun getName(): String
}