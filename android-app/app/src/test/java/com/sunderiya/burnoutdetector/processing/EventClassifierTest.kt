
package com.sunderiya.burnoutdetector.processing

import org.junit.Assert.assertEquals
import org.junit.Test

class EventClassifierTest {

    @Test
    fun singleCharacterAdditionIsTyping() {
        assertEquals(
            TextChangeType.TYPED_CHARACTER,
            EventClassifier.classifyTextChange(1, 0)
        )
    }

    @Test
    fun positiveOneNetChangeIsTyping() {
        assertEquals(
            TextChangeType.TYPED_CHARACTER,
            EventClassifier.classifyTextChange(5, 4)
        )
    }

    @Test
    fun singleCharacterRemovalIsDeletion() {
        assertEquals(
            TextChangeType.DELETION,
            EventClassifier.classifyTextChange(0, 1)
        )
    }

    @Test
    fun negativeOneNetChangeIsDeletion() {
        assertEquals(
            TextChangeType.DELETION,
            EventClassifier.classifyTextChange(4, 5)
        )
    }

    @Test
    fun equalCountsAreIgnored() {
        assertEquals(
            TextChangeType.IGNORED,
            EventClassifier.classifyTextChange(5, 5)
        )
    }

    @Test
    fun zeroCountsAreIgnored() {
        assertEquals(
            TextChangeType.IGNORED,
            EventClassifier.classifyTextChange(0, 0)
        )
    }

    @Test
    fun multipleCharacterAdditionIsReplacement() {
        assertEquals(
            TextChangeType.REPLACEMENT,
            EventClassifier.classifyTextChange(8, 0)
        )
    }

    @Test
    fun multipleCharacterRemovalIsReplacement() {
        assertEquals(
            TextChangeType.REPLACEMENT,
            EventClassifier.classifyTextChange(0, 5)
        )
    }

    @Test
    fun mixedCountsWithLargeNetChangeAreReplacement() {
        assertEquals(
            TextChangeType.REPLACEMENT,
            EventClassifier.classifyTextChange(8, 2)
        )
    }

    @Test
    fun negativeAddedCountIsInvalid() {
        assertEquals(
            TextChangeType.INVALID,
            EventClassifier.classifyTextChange(-1, 0)
        )
    }

    @Test
    fun negativeRemovedCountIsInvalid() {
        assertEquals(
            TextChangeType.INVALID,
            EventClassifier.classifyTextChange(1, -1)
        )
    }

    @Test
    fun bothNegativeCountsAreInvalid() {
        assertEquals(
            TextChangeType.INVALID,
            EventClassifier.classifyTextChange(-1, -1)
        )
    }

    @Test
    fun maximumIntegerCountIsHandled() {
        assertEquals(
            TextChangeType.REPLACEMENT,
            EventClassifier.classifyTextChange(Int.MAX_VALUE, 0)
        )
    }
}
