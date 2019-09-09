package io.pleo.antaeus.core.workers

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

suspend fun worker(id: Int, queue: ReceiveChannel<Int>) {
    for (item in queue) {
        delay(3000L)
        when (item) {
            0 -> println("IT WAS ZERO")
            else -> println("IT WASN'T ZERO")
        }
    }
}

class BillingWorker(
    private val queue: ReceiveChannel<Int>,
    private val numJobs: Int
) : Runnable {
    companion object {
        fun spawn(queue: ReceiveChannel<Int>, numJobs: Int = 4) {
            val worker = BillingWorker(queue, numJobs)
            val t = Thread(worker)
            t.isDaemon = true
            t.start()
        }
    }

    override fun run() = runBlocking {
        repeat(numJobs) {
            println("launching worker $it")
            launch { worker(it, queue) }
        }
    }
}