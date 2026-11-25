
package com.sunderiya.burnoutdetector

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.consentDataStore by
preferencesDataStore(name = "consent_settings")

class ConsentPreferences(context: Context) {

    private val appContext = context.applicationContext

    companion object {

        private val CONSENT_ACCEPTED_AT =
            longPreferencesKey("consent_accepted_at")

        private val MONITORING_ENABLED =
            booleanPreferencesKey("monitoring_enabled")

        private val preferenceMutex = Mutex()
    }

    val consentTimestamp: Flow<Long?> =
        appContext.consentDataStore.data.map { preferences ->
            preferences[CONSENT_ACCEPTED_AT]
        }

    private val savedMonitoringEnabled: Flow<Boolean> =
        appContext.consentDataStore.data.map { preferences ->

            preferences[CONSENT_ACCEPTED_AT] != null &&
                    (preferences[MONITORING_ENABLED] ?: false)
        }

    val monitoringEnabled: Flow<Boolean> =
        combine(
            savedMonitoringEnabled,
            MonitoringGate.allowed
        ) { saved, gateAllowed ->

            saved && gateAllowed
        }

    suspend fun saveConsent() {

        MonitoringGate.close()

        preferenceMutex.withLock {

            appContext.consentDataStore.edit { preferences ->

                preferences[CONSENT_ACCEPTED_AT] =
                    System.currentTimeMillis()

                preferences[MONITORING_ENABLED] = false
            }
        }
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {

        val request = MonitoringGate.close()

        preferenceMutex.withLock {

            val updatedPreferences =
                appContext.consentDataStore.edit { preferences ->

                    val hasConsent =
                        preferences[CONSENT_ACCEPTED_AT] != null

                    preferences[MONITORING_ENABLED] =
                        enabled && hasConsent
                }

            val hasConsent =
                updatedPreferences[CONSENT_ACCEPTED_AT] != null

            val savedEnabled =
                updatedPreferences[MONITORING_ENABLED] ?: false

            if (enabled && hasConsent && savedEnabled) {

                MonitoringGate.openIfCurrent(request)
            }
        }
    }

    suspend fun clearConsent() {

        MonitoringGate.close()

        preferenceMutex.withLock {

            appContext.consentDataStore.edit { preferences ->

                preferences[MONITORING_ENABLED] = false

                preferences.remove(CONSENT_ACCEPTED_AT)
            }
        }
    }
}
