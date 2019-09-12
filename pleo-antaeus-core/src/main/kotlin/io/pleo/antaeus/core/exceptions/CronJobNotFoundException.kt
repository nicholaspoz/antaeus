package io.pleo.antaeus.core.exceptions

import java.lang.Exception

// Exception structure could be refactored
class CronJobNotFoundException(name: String) : EntityNotFoundException(name, 0)
