
package com.sunderiya.burnoutdetector

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MonitoringGate {

    private val _allowed = MutableStateFlow(false)

    val allowed = _allowed.asStateFlow()

    private var generation = 0L

    fun isAllowed(): Boolean {
        return _allowed.value
    }

    @Synchronized
    fun close(): Long {
        generation++
        _allowed.value = false
        return generation
    }

    @Synchronized
    fun openIfCurrent(request: Long): Boolean {

        if (request != generation) {
            return false
        }

        _allowed.value = true
        return true
    }
}
