package io.pleo.antaeus.models

import java.math.BigDecimal

enum class Currency(val subscriptionPrice: BigDecimal) {
    EUR(BigDecimal(18)),
    USD(BigDecimal(20)),
    DKK(BigDecimal(135)),
    SEK(BigDecimal(200)),
    GBP(BigDecimal(15))
}
