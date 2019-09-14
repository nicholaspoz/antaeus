/*
    Implements endpoints related to customers.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Charge
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice

class CustomerService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Customer> {
        return dal.fetchCustomers()
    }

    fun fetch(id: Int): Customer {
        return dal.fetchCustomer(id) ?: throw CustomerNotFoundException(id)
    }

    fun notifyOfChargeFailure(customer: Customer, charge: Charge) {
        // placeholder for sending a notification to the customer
    }
}
