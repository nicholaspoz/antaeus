package io.pleo.antaeus.core.jobs

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkObject
import io.mockk.verify
import io.pleo.antaeus.core.getSubscriptionAmount
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
import org.junit.jupiter.api.BeforeEach

class MonthlyBillingJobRunnerTest {
    // Constants
    private val name = "MONTHLY_BILLING_2019_09"
    private val period = DateTime(
        2019, 9, 13, 20, 30, DateTimeZone.UTC
    )
    private val currencyDkk = Currency.DKK
    private val amount = getSubscriptionAmount(currencyDkk)

    // Mocked dependencies
    private val customer = mockk<Customer> { every { currency } returns currencyDkk }
    private val invoice = mockk<Invoice>()
    private val charge = mockk<Charge>()
    private val customerService = mockk<CustomerService> {
        every { fetchAll() } returns listOf(customer, customer, customer)
    }
    private val invoiceService = mockk<InvoiceService> {
        every {
            create(
                item = name,
                customer = customer,
                amount = amount,
                status = InvoiceStatus.PENDING
            )
        } returns invoice
        every { updateStatus(invoice, InvoiceStatus.PAID) } returns invoice
    }
    private val chargeService = mockk<ChargeService> {
        every { createFromInvoice(customer, invoice) } returns charge
    }

    // Object under test
    private val jobRunner = MonthlyBillingJobRunner(
        customerService = customerService,
        invoiceService = invoiceService,
        chargeService = chargeService,
        period = period
    )

    @BeforeEach
    fun beforeEach() {
        unmockkObject(charge)
    }

    @Test
    fun `will generate name from type and period`() {
        assertEquals(name, jobRunner.name)
    }

    @Test
    fun `will create new invoices for each customer`() {
        every { charge.status } returns ChargeStatus.PAID
        jobRunner.run()
        verify(exactly = 3) {
            invoiceService.create(
                item = name,
                customer = customer,
                amount = getSubscriptionAmount(currencyDkk),
                status = InvoiceStatus.PENDING
            )
        }
    }

    @Test
    fun `will charge invoices`() {
        every { charge.status } returns ChargeStatus.PAID
        jobRunner.run()
        verify(exactly = 3) { chargeService.createFromInvoice(customer, invoice) }
    }

    @Test
    fun `will update invoice status`() {
        every { charge.status } returns ChargeStatus.PAID
        jobRunner.run()
        verify(exactly = 3) { invoiceService.updateStatus(invoice, InvoiceStatus.PAID) }
    }

    @Test
    fun `will notify customer of charge failure`() {
        every { charge.status } returns ChargeStatus.FAILED
        every { customerService.notifyOfChargeFailure(charge) } just Runs
        jobRunner.run()
        verify(exactly = 3) { customerService.notifyOfChargeFailure(charge) }
    }
}
