
package com.sunderiya.burnoutdetector

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BurnoutAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BurnoutService"
    }

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    private var observerJob: Job? = null

    private var monitoringAllowed = false

    override fun onServiceConnected() {

        super.onServiceConnected()

        monitoringAllowed = false

        observerJob?.cancel()

        Log.d(TAG, "Accessibility service connected")

        val preferences =
            ConsentPreferences(applicationContext)

        observerJob = serviceScope.launch {

            Log.d(TAG, "DataStore observer started")

            try {

                preferences.monitoringEnabled.collect { allowed ->

                    monitoringAllowed = allowed

                    Log.d(
                        TAG,
                        "DataStore emitted: allowed=$allowed"
                    )
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                monitoringAllowed = false

                MonitoringGate.close()

                Log.e(TAG, "Monitoring observer failed", e)
            }
        }
    }

    private fun shouldProcessEvent(
        event: AccessibilityEvent
    ): Boolean {

        // Immediate shared gate check.
        if (!MonitoringGate.isAllowed()) return false

        // Additional observed permission check.
        if (!monitoringAllowed) return false

        // Reject password-related events.
        if (event.isPassword) return false

        return PrivacyEventFilter.shouldProcess(
            monitoringAllowed = monitoringAllowed,
            isPassword = event.isPassword,
            packageName = event.packageName?.toString(),
            ownPackageName = applicationContext.packageName,
            eventType = event.eventType
        )
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) return

        if (!shouldProcessEvent(event)) return

        when (event.eventType) {

            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                // Month 3: sanitized typing metadata.
            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                // Month 3: sanitized scrolling metadata.
            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // Month 3: ephemeral app-switch counting.
            }
        }
    }

    override fun onInterrupt() {

        monitoringAllowed = false

        MonitoringGate.close()

        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {

        monitoringAllowed = false

        MonitoringGate.close()

        observerJob?.cancel()

        serviceScope.cancel()

        Log.d(TAG, "Accessibility service destroyed")

        super.onDestroy()
    }
}
