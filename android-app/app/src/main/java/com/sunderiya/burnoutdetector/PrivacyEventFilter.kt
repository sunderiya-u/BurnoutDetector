
package com.sunderiya.burnoutdetector

import android.view.accessibility.AccessibilityEvent

object PrivacyEventFilter {

    private val excludedPackages = setOf(
        "com.android.systemui",
        "com.android.settings",
        "com.samsung.android.app.settings",
        "com.google.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.swiftkey.swiftkeyconfigurator",
        "com.touchtype.swiftkey"
    )

    fun shouldProcess(
        monitoringAllowed: Boolean,
        isPassword: Boolean,
        packageName: String?,
        ownPackageName: String,
        eventType: Int
    ): Boolean {

        if (!monitoringAllowed) return false

        if (isPassword) return false

        if (packageName.isNullOrBlank()) return false

        if (packageName == ownPackageName) return false

        if (packageName in excludedPackages) return false

        return when (eventType) {

            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> true

            else -> false
        }
    }
}
