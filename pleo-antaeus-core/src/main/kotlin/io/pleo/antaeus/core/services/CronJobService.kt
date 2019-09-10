package io.pleo.antaeus.core.services

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus

class CronJobService(private val dal: AntaeusDal) {
    fun updateStatusByName(name: String, status: CronJobStatus): CronJob {
        return dal.updateCronJobStatusByName(name, status)
    }

    fun getOrCreateCronJob(name: String): CronJob {
        return dal.getOrCreateCronJob(name = name)
    }
}
