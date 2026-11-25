
package com.sunderiya.burnoutdetector

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test

class MonitoringGateTest {

    @Before
    fun setUp() {
        MonitoringGate.close()
    }

    @After
    fun tearDown() {
        MonitoringGate.close()
    }

    @Test
    fun gateIsClosedAfterReset() {
        assertFalse(MonitoringGate.isAllowed())
    }

    @Test
    fun gateOpensWithCurrentRequest() {
        val request = MonitoringGate.close()

        assertTrue(MonitoringGate.openIfCurrent(request))
        assertTrue(MonitoringGate.isAllowed())
    }

    @Test
    fun gateClosesImmediately() {
        val request = MonitoringGate.close()
        MonitoringGate.openIfCurrent(request)

        MonitoringGate.close()

        assertFalse(MonitoringGate.isAllowed())
    }

    @Test
    fun staleEnableRequestCannotReopenGate() {
        val oldRequest = MonitoringGate.close()

        MonitoringGate.close()

        assertFalse(
            MonitoringGate.openIfCurrent(oldRequest)
        )

        assertFalse(MonitoringGate.isAllowed())
    }

    @Test
    fun repeatedPauseKeepsGateClosed() {
        MonitoringGate.close()
        MonitoringGate.close()
        MonitoringGate.close()

        assertFalse(MonitoringGate.isAllowed())
    }

    @Test
    fun newEnableRequestCanReopenGate() {
        val oldRequest = MonitoringGate.close()

        MonitoringGate.close()

        val newRequest = MonitoringGate.close()

        assertFalse(
            MonitoringGate.openIfCurrent(oldRequest)
        )

        assertTrue(
            MonitoringGate.openIfCurrent(newRequest)
        )

        assertTrue(MonitoringGate.isAllowed())
    }
}
