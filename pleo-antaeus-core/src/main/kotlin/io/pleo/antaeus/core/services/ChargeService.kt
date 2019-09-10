package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.factories.PaymentProviderFactory
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.ChargeStatus
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.Money

class ChargeService(
    private val dal: AntaeusDal,
    private val paymentProviderFactory: PaymentProviderFactory
) {

    fun create(customer: Customer, invoice: Invoice, amount: Money): Charge {
        if (customer.currency != invoice.amount.currency) {
            throw CurrencyMismatchException(invoice.id, customer.id)
        }
        val status = callExternalPaymentProvider(customer, invoice)
        return dal.createCharge(invoice, amount, status)
    }

    private fun callExternalPaymentProvider(customer: Customer, invoice: Invoice): ChargeStatus {
        val paymentProvider = paymentProviderFactory.getPaymentProvider(customer)
        return try {
            paymentProvider.charge(invoice)
        } catch (e: CustomerNotFoundException) {
            // TODO Log that an error happened
            ChargeStatus.FAILED
        } catch (e: NetworkException) {
            // TODO Log that an error happened
            ChargeStatus.FAILED
        }
    }
}