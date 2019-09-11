package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal>()
    private val invoiceService = InvoiceService(dal = dal)

    @AfterEach
    fun after() = unmockkAll()

    @Test
    fun `will throw if invoice is not found`() {
        every { dal.fetchInvoice(404) } returns null
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `will return invoice`() {
        val expected = mockk<Invoice>()
        every { dal.fetchInvoice(200) } returns expected
        assertEquals(expected, invoiceService.fetch(200))
    }
}
