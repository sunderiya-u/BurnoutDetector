
package com.sunderiya.burnoutdetector.processing

data class TypingSnapshot(
    val typedCharCount: Int,
    val deletionCount: Int,
    val replacementCount: Int,
    val activeTypingMs: Long,
    val invalidEventCount: Int
)

class TypingTracker(
    private val maxTypingIntervalMs: Long = 2000L
) {

    init {
        require(maxTypingIntervalMs > 0)
    }

    private var typedCharCount = 0
    private var deletionCount = 0
    private var replacementCount = 0
    private var activeTypingMs = 0L
    private var invalidEventCount = 0

    private var lastTypingTimestamp: Long? = null

    fun process(
        type: TextChangeType,
        timestampMs: Long
    ) {

        if (timestampMs < 0) {
            invalidEventCount++
            return
        }

        when (type) {

            TextChangeType.TYPED_CHARACTER -> {
                typedCharCount++
                updateTypingDuration(timestampMs)
            }

            TextChangeType.DELETION -> {
                deletionCount++
                updateTypingDuration(timestampMs)
            }

            TextChangeType.REPLACEMENT -> {
                replacementCount++
            }

            TextChangeType.INVALID -> {
                invalidEventCount++
            }

            TextChangeType.IGNORED -> {
                // No counters are updated.
            }
        }
    }

    private fun updateTypingDuration(
        timestampMs: Long
    ) {
        val previousTimestamp = lastTypingTimestamp

        if (previousTimestamp != null) {

            // Do not move the timestamp backwards.
            if (timestampMs < previousTimestamp) {
                return
            }

            val interval = timestampMs - previousTimestamp

            if (interval <= maxTypingIntervalMs) {
                activeTypingMs += interval
            }
        }

        lastTypingTimestamp = timestampMs
    }

    fun snapshot(): TypingSnapshot {
        return TypingSnapshot(
            typedCharCount = typedCharCount,
            deletionCount = deletionCount,
            replacementCount = replacementCount,
            activeTypingMs = activeTypingMs,
            invalidEventCount = invalidEventCount
        )
    }
    fun breakTiming() {
        lastTypingTimestamp = null
    }

    fun reset() {
        typedCharCount = 0
        deletionCount = 0
        replacementCount = 0
        activeTypingMs = 0L
        invalidEventCount = 0
        lastTypingTimestamp = null
    }
}
