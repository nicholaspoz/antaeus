package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal>()

    private val invoiceService = InvoiceService(dal = dal)

    @BeforeEach
    fun beforeEach() {
        unmockkAll()
    }

    @Test
    fun `will throw if invoice is not found`() {
        every { dal.fetchInvoice(404) } returns null
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `will create an invoice`() {
        val item = "item"
        val amount = mockk<Money>()
        val customer = mockk<Customer>()
        val status = InvoiceStatus.PENDING
        val response = mockk<Invoice>()
        every { dal.createInvoice(item, amount, customer, status) } returns response
        assertEquals(response, invoiceService.create(item, amount, customer, status))
    }

    @Test
    fun `will update status`() {
        val invoice = mockk<Invoice>()
        val status = InvoiceStatus.PENDING
        val response = mockk<Invoice>()
        every { dal.updateInvoiceStatus(invoice, status) } returns response
        assertEquals(response, invoiceService.updateStatus(invoice, status))
    }
}
