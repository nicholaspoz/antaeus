package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CronJobNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus

class CronJobService(private val dal: AntaeusDal) {
    fun getOrCreateByName(name: String): CronJob {
        return dal.getOrCreateCronJobByName(name) ?: throw CronJobNotFoundException(name)
    }

    fun updateStatusByName(name: String, status: CronJobStatus): CronJob {
        return dal.updateCronJobStatusByName(name, status)
            ?: throw CronJobNotFoundException(name)
    }
}
