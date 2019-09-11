package io.pleo.antaeus.core.services

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import io.pleo.antaeus.core.factories.BillingJobFactory
import io.pleo.antaeus.core.jobs.JobRunner
import io.pleo.antaeus.core.jobs.JobType
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class BillingServiceTest {
    private val billingJobFactory = mockk<BillingJobFactory>()
    private val cronJobService = mockk<CronJobService>()
    private val jobRunner = mockk<JobRunner>(relaxUnitFun = true)
    private val billingService = BillingService(billingJobFactory, cronJobService)
    private val cronJob = mockk<CronJob>()

    @BeforeEach
    fun beforeEach() {
        unmockkAll()
    }

    @Test
    fun `will create a thread for a new job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.CREATED
        every { billingJobFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.getName() } returns jobName
        every { cronJobService.getOrCreateCronJob(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.startJob(jobType, period)
        assertNotNull(resultThread)
    }

    @Test
    fun `will run a new job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.CREATED
        every { billingJobFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.getName() } returns jobName
        every { cronJobService.getOrCreateCronJob(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.startJob(jobType, period)
        resultThread?.join()
        verify(exactly = 1) { jobRunner.run() }
        verify(exactly = 3) { jobRunner.getName() }
        confirmVerified(jobRunner)
    }

    @Test
    fun `will update the status of a job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.CREATED
        every { billingJobFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.getName() } returns jobName
        every { cronJobService.getOrCreateCronJob(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.startJob(jobType, period)
        resultThread?.join()
        verify(exactly = 1) { cronJobService.getOrCreateCronJob(jobName)}
        verify(exactly = 1) {cronJobService.updateStatusByName(jobName, CronJobStatus.RUNNING)}
        verify(exactly = 1) {cronJobService.updateStatusByName(jobName, CronJobStatus.FINISHED)}
    }

    @Test
    fun `will not create a thread for a running job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.RUNNING
        every { billingJobFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.getName() } returns jobName
        every { cronJobService.getOrCreateCronJob(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.startJob(jobType, period)
        assertNull(resultThread)
    }

    @Test
    fun `will not create a thread for a finished job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.FINISHED
        every { billingJobFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.getName() } returns jobName
        every { cronJobService.getOrCreateCronJob(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.startJob(jobType, period)
        assertNull(resultThread)
    }
}