
package com.sunderiya.burnoutdetector.processing

/**
 * Categories derived from numerical text-change metadata.
 *
 * These categories are behavioral proxies, not verified
 * physical keyboard actions.
 */
enum class TextChangeType {
    TYPED_CHARACTER,
    DELETION,
    REPLACEMENT,
    IGNORED,
    INVALID
}

/**
 * Classifies text-change events without accessing text,
 * keyboard content, or accessibility node information.
 */
object EventClassifier {

    fun classifyTextChange(
        addedCount: Int,
        removedCount: Int
    ): TextChangeType {

        // Reject malformed or unavailable counts.
        if (addedCount < 0 || removedCount < 0) {
            return TextChangeType.INVALID
        }

        val netChange = addedCount - removedCount

        return when (netChange) {
            1 -> TextChangeType.TYPED_CHARACTER
            -1 -> TextChangeType.DELETION
            0 -> TextChangeType.IGNORED
            else -> TextChangeType.REPLACEMENT
        }
    }
}
