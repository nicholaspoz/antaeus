package io.pleo.antaeus.app

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import kotlin.random.Random

// This will create all schemas and setup initial data
internal fun setupInitialData(dal: AntaeusDal) {
    (1..1000).mapNotNull {
        dal.createCustomer(
            currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
        )
    }
}
