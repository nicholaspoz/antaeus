package io.pleo.antaeus.core

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Money

/*
 A placeholder for more a more robust logging configuration
 */
object logger {
    fun debug(msg: String) = println("[DEBUG]: $msg")
    fun info(msg: String) = println("[INFO]: $msg")
    fun warn(msg: String) = println("[WARN]: $msg")
    fun error(msg: String) = println("[ERROR]: $msg")
}

fun getSubscriptionAmount(currency: Currency) = Money(
    value = currency.subscriptionPrice,
    currency = currency
)
