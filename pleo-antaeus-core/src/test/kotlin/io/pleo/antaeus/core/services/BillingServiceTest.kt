package io.pleo.antaeus.core.services

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import io.pleo.antaeus.core.jobs.JobRunnerFactory
import io.pleo.antaeus.core.jobs.JobRunner
import io.pleo.antaeus.core.jobs.JobType
import io.pleo.antaeus.models.CronJob
import io.pleo.antaeus.models.CronJobStatus
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class BillingServiceTest {
    private val jobRunnerFactory = mockk<JobRunnerFactory>()
    private val cronJobService = mockk<CronJobService>()
    private val jobRunner = mockk<JobRunner>(relaxUnitFun = true)
    private val billingService = BillingService(cronJobService, jobRunnerFactory)
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
        every { jobRunnerFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.name } returns jobName
        every { cronJobService.getOrCreateByName(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.runBillingJob(jobType, period)
        assertNotNull(resultThread)
    }

    @Test
    fun `will run a new job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.CREATED
        every { jobRunnerFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.name } returns jobName
        every { cronJobService.getOrCreateByName(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.runBillingJob(jobType, period)
        resultThread?.join()
        verify(exactly = 1) { jobRunner.run() }
        verify(exactly = 3) { jobRunner.name }
        confirmVerified(jobRunner)
    }

    @Test
    fun `will update the status of a job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.CREATED
        every { jobRunnerFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.name } returns jobName
        every { cronJobService.getOrCreateByName(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.runBillingJob(jobType, period)
        resultThread?.join()
        verify(exactly = 1) { cronJobService.getOrCreateByName(jobName) }
        verify(exactly = 1) {
            cronJobService.updateStatusByName(
                jobName,
                CronJobStatus.RUNNING
            )
        }
        verify(exactly = 1) {
            cronJobService.updateStatusByName(
                jobName,
                CronJobStatus.FINISHED
            )
        }
    }

    @Test
    fun `will not create a thread for a running job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.RUNNING
        every { jobRunnerFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.name } returns jobName
        every { cronJobService.getOrCreateByName(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.runBillingJob(jobType, period)
        assertNull(resultThread)
    }

    @Test
    fun `will not create a thread for a finished job`() {
        val jobName = "jobName"
        val jobType = JobType.MONTHLY_BILLING
        val period = DateTime.now()

        every { cronJob.status } returns CronJobStatus.FINISHED
        every { jobRunnerFactory.getJobRunner(jobType, period) } returns jobRunner
        every { jobRunner.name } returns jobName
        every { cronJobService.getOrCreateByName(jobName) } returns cronJob
        every { cronJobService.updateStatusByName(jobName, any()) } returns cronJob

        val (_, resultThread) = billingService.runBillingJob(jobType, period)
        assertNull(resultThread)
    }
}
