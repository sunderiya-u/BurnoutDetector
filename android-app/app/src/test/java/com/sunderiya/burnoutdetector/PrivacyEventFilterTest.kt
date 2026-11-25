
package com.sunderiya.burnoutdetector

import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyEventFilterTest {

    private val ownPackage =
        "com.sunderiya.burnoutdetector"

    private fun checkEvent(
        monitoring: Boolean = true,
        password: Boolean = false,
        app: String? = "com.example.testapp",
        type: Int = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
    ): Boolean {

        return PrivacyEventFilter.shouldProcess(
            monitoringAllowed = monitoring,
            isPassword = password,
            packageName = app,
            ownPackageName = ownPackage,
            eventType = type
        )
    }

    @Test
    fun rejectsWhenMonitoringDisabled() {
        assertFalse(checkEvent(monitoring = false))
    }

    @Test
    fun rejectsPasswordEvents() {
        assertFalse(checkEvent(password = true))
    }

    @Test
    fun rejectsMissingPackageName() {
        assertFalse(checkEvent(app = null))
    }

    @Test
    fun rejectsBlankPackageName() {
        assertFalse(checkEvent(app = ""))
    }

    @Test
    fun rejectsOwnApplication() {
        assertFalse(checkEvent(app = ownPackage))
    }

    @Test
    fun rejectsSystemUI() {
        assertFalse(
            checkEvent(app = "com.android.systemui")
        )
    }

    @Test
    fun rejectsAndroidSettings() {
        assertFalse(
            checkEvent(app = "com.android.settings")
        )
    }

    @Test
    fun rejectsSamsungKeyboard() {
        assertFalse(
            checkEvent(
                app = "com.samsung.android.honeyboard"
            )
        )
    }

    @Test
    fun rejectsGoogleKeyboard() {
        assertFalse(
            checkEvent(
                app = "com.google.android.inputmethod.latin"
            )
        )
    }

    @Test
    fun rejectsUnsupportedEventType() {
        assertFalse(
            checkEvent(
                type = AccessibilityEvent.TYPE_VIEW_CLICKED
            )
        )
    }

    @Test
    fun acceptsTypingEvent() {
        assertTrue(
            checkEvent(
                type = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            )
        )
    }

    @Test
    fun acceptsScrollingEvent() {
        assertTrue(
            checkEvent(
                type = AccessibilityEvent.TYPE_VIEW_SCROLLED
            )
        )
    }

    @Test
    fun acceptsWindowStateEvent() {
        assertTrue(
            checkEvent(
                type = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            )
        )
    }
}
