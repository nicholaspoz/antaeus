package io.pleo.antaeus.core.jobs

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.services.ChargeService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class WeeklySettlementJobRunnerTest {
    private val name = "WEEKLY_SETTLEMENT_2019_39"
    private val period = DateTime(
        2019, 9, 28, 0, 0, DateTimeZone.UTC
    )

    // Mocked dependencies
    private val customer = mockk<Customer>()
    private val invoice = mockk<Invoice> { every { customerId } returns 1 }
    private val charge = mockk<Charge>()
    private val customerService = mockk<CustomerService> {
        every { fetch(1) } returns customer
        every { notifyOfChargeFailure(customer, charge) } just Runs
    }
    private val invoiceService = mockk<InvoiceService> {
        every {
            fetchPendingAfter(any())
        } returns listOf(invoice, invoice, invoice)
        every { updateStatus(invoice, any()) } returns invoice
    }
    private val chargeService = mockk<ChargeService> {
        every { createFromInvoice(customer, invoice) } returns charge
    }

    // Object under test
    private val jobRunner = WeeklySettlementJobRunner(
        customerService = customerService,
        invoiceService = invoiceService,
        chargeService = chargeService,
        period = period
    )

    @Test
    fun `will generate name from type and period`() {
        assertEquals(name, jobRunner.name)
    }

    @Test
    fun `will charge pending invoices`() {
        every { charge.status } returns ChargeStatus.PAID
        jobRunner.run()
        verify(exactly = 3) {
            chargeService.createFromInvoice(customer, invoice)
        }
    }

    @Test
    fun `will complete paid invoices`() {
        every { charge.status } returns ChargeStatus.PAID
        jobRunner.run()
        verify(exactly = 3) {
            invoiceService.updateStatus(invoice, InvoiceStatus.PAID)
        }
    }

    @Test
    fun `will fail invoices at the end of the month`() {
        every { charge.status } returns ChargeStatus.FAILED
        jobRunner.run()
        verify(exactly = 3) {
            invoiceService.updateStatus(invoice, InvoiceStatus.FAILED)
        }
    }
}
