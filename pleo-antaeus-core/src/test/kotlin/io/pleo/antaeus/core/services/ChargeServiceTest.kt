package io.pleo.antaeus.core.services

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkObject
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.external.PaymentProviderFactory
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ChargeServiceTest {
    private val dal = mockk<AntaeusDal>()
    private val paymentProvider = mockk<PaymentProvider>()
    private val paymentProviderFactory = mockk<PaymentProviderFactory> {
        every { getPaymentProvider(any()) } returns paymentProvider
    }
    private val chargeService = ChargeService(dal, paymentProviderFactory)
    private val customer = mockk<Customer>()
    private val invoice = mockk<Invoice>()
    private val charge = mockk<Charge>()

    @BeforeEach
    fun beforeEach() {
        unmockkObject(dal, paymentProvider, invoice, charge)
    }

    @Test
    fun `will create a Charge`() {
        every { paymentProvider.charge(invoice) } returns ChargeStatus.PAID
        every { dal.createChargeFromInvoice(invoice, ChargeStatus.PAID) } returns charge
        assertEquals(charge, chargeService.createFromInvoice(customer, invoice))
    }

    @Test
    fun `will create a failed charge when CustomerNotFound`() {
        every { paymentProvider.charge(invoice) } throws CustomerNotFoundException(0)
        every { dal.createChargeFromInvoice(invoice, ChargeStatus.FAILED) } returns charge

        chargeService.createFromInvoice(customer, invoice)
        verify(exactly = 1) { dal.createChargeFromInvoice(invoice, ChargeStatus.FAILED) }
        confirmVerified(dal)
    }

    @Test
    fun `will create a failed charge when CurrencyMismatchException`() {
        every { paymentProvider.charge(invoice) } throws CurrencyMismatchException(0, 1)
        every { dal.createChargeFromInvoice(invoice, ChargeStatus.FAILED) } returns charge

        chargeService.createFromInvoice(customer, invoice)
        verify(exactly = 1) { dal.createChargeFromInvoice(invoice, ChargeStatus.FAILED) }
        confirmVerified(dal)
    }

    @Test
    fun `will create a failed charge when NetworkException`() {
        every { paymentProvider.charge(invoice) } throws CurrencyMismatchException(0, 1)
        every { dal.createChargeFromInvoice(invoice, ChargeStatus.FAILED) } returns charge

        chargeService.createFromInvoice(customer, invoice)
        verify(exactly = 1) { dal.createChargeFromInvoice(invoice, ChargeStatus.FAILED) }
        confirmVerified(dal)
    }

}
