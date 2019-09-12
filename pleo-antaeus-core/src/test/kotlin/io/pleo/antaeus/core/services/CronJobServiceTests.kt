package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.pleo.antaeus.core.exceptions.CronJobNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.CronJobStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CronJobServiceTests {
    private val dal = mockk<AntaeusDal>()
    private val cronJobService = CronJobService(dal)

    @BeforeEach
    fun beforeEach() {
        unmockkAll()
    }

    @Test
    fun `getOrCreateByName will throw if result is null`() {
        val name = "name"
        every { dal.getOrCreateCronJobByName(name) } returns null
        assertThrows<CronJobNotFoundException> {
            cronJobService.getOrCreateByName(name)
        }
    }

    @Test
    fun `updateStatusByName will throw if result is null`() {
        val name = "name"
        val status = CronJobStatus.CREATED
        every { dal.updateCronJobStatusByName(name, status) } returns null
        assertThrows<CronJobNotFoundException> {
            cronJobService.updateStatusByName(name, status)
        }
    }
}
