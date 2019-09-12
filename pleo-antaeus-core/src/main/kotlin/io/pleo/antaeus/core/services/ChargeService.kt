package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProviderFactory
import io.pleo.antaeus.core.logger
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice

class ChargeService(
    private val dal: AntaeusDal,
    private val paymentProviderFactory: PaymentProviderFactory
) {
    fun createFromInvoice(customer: Customer, invoice: Invoice): Charge {
        val status = callExternalPaymentProvider(customer, invoice)
        return dal.createChargeFromInvoice(invoice, status)
    }

    private fun callExternalPaymentProvider(
        customer: Customer,
        invoice: Invoice
    ): ChargeStatus {
        val paymentProvider = paymentProviderFactory.getPaymentProvider(customer)
        return try {
            paymentProvider.charge(invoice)
        } catch (e: CustomerNotFoundException) {
            logger.error("Customer not found in external payment provider.")
            ChargeStatus.FAILED
        } catch (e: CurrencyMismatchException) {
            logger.error("Could not charge invoice due to currency mismatch.")
            ChargeStatus.FAILED
        } catch (e: NetworkException) {
            logger.error("Encountered a networking error calling payment provider.")
            ChargeStatus.FAILED
        }
    }
}
