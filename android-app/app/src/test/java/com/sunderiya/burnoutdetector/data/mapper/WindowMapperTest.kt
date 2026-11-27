
package com.sunderiya.burnoutdetector.data.mapper

import com.sunderiya.burnoutdetector.aggregation.WindowSummary
import org.junit.Assert.*
import org.junit.Test

class WindowMapperTest {

    private fun sampleSummary() = WindowSummary(
        startTime = 300_000L,
        endTime = 600_000L,
        utcOffsetMinutes = 60,
        monitoredMs = 300_000L,
        isComplete = true,
        eventCount = 25,
        invalidEventCount = 2,
        typedCharCount = 120,
        deletionCount = 30,
        replacementCount = 4,
        activeTypingMs = 30_000L,
        typingSpeed = 240.0,
        correctionRatio = 0.2,
        scrollGestureCount = 5,
        scrollActiveMs = 4_000L,
        scrollVariability = 120.0,
        scrollDistancePx = 2500L,
        appSwitchCount = 3
    )

    @Test
    fun mapsAllFieldsCorrectly() {

        val summary = sampleSummary()
        val entity = WindowMapper.toEntity(summary)

        assertEquals(summary.startTime, entity.startTime)
        assertEquals(summary.endTime, entity.endTime)
        assertEquals(summary.utcOffsetMinutes, entity.utcOffsetMinutes)
        assertEquals(summary.monitoredMs, entity.monitoredMs)
        assertEquals(summary.isComplete, entity.isComplete)

        assertEquals(summary.eventCount, entity.eventCount)
        assertEquals(summary.invalidEventCount, entity.invalidEventCount)

        assertEquals(summary.typedCharCount, entity.typedCharCount)
        assertEquals(summary.deletionCount, entity.deletionCount)
        assertEquals(summary.replacementCount, entity.replacementCount)
        assertEquals(summary.activeTypingMs, entity.activeTypingMs)
        assertEquals(summary.typingSpeed, entity.typingSpeed)
        assertEquals(summary.correctionRatio, entity.correctionRatio)

        assertEquals(summary.scrollGestureCount, entity.scrollGestureCount)
        assertEquals(summary.scrollActiveMs, entity.scrollActiveMs)
        assertEquals(summary.scrollVariability, entity.scrollVariability)
        assertEquals(summary.scrollDistancePx, entity.scrollDistancePx)

        assertEquals(summary.appSwitchCount, entity.appSwitchCount)
    }

    @Test
    fun primaryKeyIsInitiallyZero() {

        val entity = WindowMapper.toEntity(sampleSummary())

        assertEquals(0L, entity.id)
    }

    @Test
    fun preservesSuppressedTypingFields() {

        val summary = sampleSummary().copy(
            typedCharCount = null,
            deletionCount = null,
            replacementCount = null,
            activeTypingMs = null,
            typingSpeed = null,
            correctionRatio = null
        )

        val entity = WindowMapper.toEntity(summary)

        assertNull(entity.typedCharCount)
        assertNull(entity.deletionCount)
        assertNull(entity.replacementCount)
        assertNull(entity.activeTypingMs)
        assertNull(entity.typingSpeed)
        assertNull(entity.correctionRatio)
    }

    @Test
    fun preservesUnavailableScrollMeasurements() {

        val summary = sampleSummary().copy(
            scrollGestureCount = 1,
            scrollVariability = null,
            scrollDistancePx = null
        )

        val entity = WindowMapper.toEntity(summary)

        assertNull(entity.scrollVariability)
        assertNull(entity.scrollDistancePx)
    }

    @Test
    fun preservesPartialWindowMetadata() {

        val summary = sampleSummary().copy(
            monitoredMs = 120_000L,
            isComplete = false
        )

        val entity = WindowMapper.toEntity(summary)

        assertEquals(120_000L, entity.monitoredMs)
        assertFalse(entity.isComplete)
        assertEquals(300_000L, entity.startTime)
        assertEquals(600_000L, entity.endTime)
    }
}
