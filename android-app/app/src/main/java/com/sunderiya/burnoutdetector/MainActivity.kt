
package com.sunderiya.burnoutdetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sunderiya.burnoutdetector.ui.theme.BurnoutDetectorTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val consentPreferences =
            ConsentPreferences(applicationContext)

        setContent {
            BurnoutDetectorTheme {
                ConsentApp(consentPreferences)
            }
        }
    }
}


@Composable
fun ConsentApp(consentPreferences: ConsentPreferences) {

    var consentTimestamp by remember {
        mutableStateOf<Long?>(null)
    }

    var monitoringEnabled by remember {
        mutableStateOf(false)
    }


    var isLoading by remember {
        mutableStateOf(true)
    }

    var storageReadError by remember {
        mutableStateOf(false)
    }

    var retryCount by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(consentPreferences, retryCount) {

        isLoading = true
        storageReadError = false

        combine(
            consentPreferences.consentTimestamp,
            consentPreferences.monitoringEnabled
        ) { timestamp, enabled ->

            Pair(timestamp, enabled)

        }.catch { error ->

            if (error is CancellationException) {
                throw error
            }

            MonitoringGate.close()

            monitoringEnabled = false
            storageReadError = true
            isLoading = false

        }.collect { (timestamp, enabled) ->

            consentTimestamp = timestamp
            monitoringEnabled = enabled
            isLoading = false
        }
    }

    val scope = rememberCoroutineScope()

    var isSaving by remember {
        mutableStateOf(false)
    }
    var isWithdrawing by remember {
        mutableStateOf(false)
    }

    var monitoringJob by remember {
        mutableStateOf<Job?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    when {

        storageReadError -> {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),

                verticalArrangement = Arrangement.Center,

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Storage error",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Could not read consent preferences. " +
                            "Monitoring has been disabled for your privacy."
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        retryCount++
                    }
                ) {
                    Text("Retry")
                }
            }
        }

        isLoading -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        consentTimestamp != null -> {

            HomeScreen(

                monitoringEnabled = monitoringEnabled,

                isSaving = isSaving,
                isWithdrawing = isWithdrawing,

                onToggleMonitoring = {

                    if (!isSaving && !isWithdrawing) {

                        val enable = !monitoringEnabled

                        if (!enable || MonitoringGate.isAllowed()) {
                            MonitoringGate.close()
                        }

                        if (!enable) {

                            MonitoringGate.close()

                            android.util.Log.d(
                                "BurnoutService",
                                "Immediate gate closed: Pause"
                            )
                        }

                        isSaving = true

                        monitoringJob = scope.launch(
                            start = CoroutineStart.UNDISPATCHED
                        ) {

                            try {

                                consentPreferences
                                    .setMonitoringEnabled(enable)

                            } catch (e: CancellationException) {

                                MonitoringGate.close()
                                throw e

                            } catch (e: Exception) {

                                MonitoringGate.close()

                                errorMessage =
                                    "Could not update monitoring preference."

                            } finally {

                                isSaving = false
                            }
                        }
                    }
                },

                onWithdraw = {

                    if (!isWithdrawing) {

                        // Immediately block event processing.
                        MonitoringGate.close()

                        isWithdrawing = true

                        android.util.Log.d(
                            "BurnoutService",
                            "Immediate gate closed: Withdraw"
                        )

                        scope.launch(
                            start = CoroutineStart.UNDISPATCHED
                        ) {

                            try {

                                // Cancel and wait for any pending
                                // monitoring preference operation.
                                monitoringJob?.cancelAndJoin()

                                monitoringJob = null

                                // Remove persistent consent.
                                consentPreferences.clearConsent()

                            } catch (e: CancellationException) {

                                MonitoringGate.close()
                                throw e

                            } catch (e: Exception) {

                                MonitoringGate.close()

                                errorMessage =
                                    "Could not clear consent. " +
                                            "Monitoring remains paused. " +
                                            "Please try again."

                            } finally {

                                isSaving = false
                                isWithdrawing = false
                            }
                        }
                    }
                }
            )
        }

        else -> {

            ConsentScreen(

                isSaving = isSaving,

                onAccept = {

                    if (!isSaving) {

                        isSaving = true

                        scope.launch(
                            start = CoroutineStart.UNDISPATCHED
                        ) {

                            try {

                                consentPreferences.saveConsent()

                            } catch (e: CancellationException) {

                                throw e

                            } catch (e: Exception) {

                                errorMessage =
                                    "Could not save consent."

                            } finally {

                                isSaving = false
                            }
                        }
                    }
                }
            )
        }
    }

    errorMessage?.let { message ->

        AlertDialog(

            onDismissRequest = {
                errorMessage = null
            },

            title = {
                Text("Storage error")
            },

            text = {
                Text(message)
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        errorMessage = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}


@Composable
fun ConsentScreen(
    isSaving: Boolean,
    onAccept: () -> Unit
) {
    var consentChecked by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Burnout Detector",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Research Consent",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            "This research application investigates " +
                    "short-term mental fatigue through " +
                    "smartphone interaction patterns."
        )

        Text(
            "Planned research metrics:",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "This prototype currently stores only your " +
                    "consent timestamp and monitoring preference. " +
                    "Behavioral data collection has not been " +
                    "implemented yet."
        )

        Text(
            "• Typing-related character counts and timing\n" +
                    "• Scrolling activity\n" +
                    "• Application switching counts\n" +
                    "• Self-reported fatigue ratings"
        )

        Text(
            "Information not collected:",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            "• Typed messages or passwords\n" +
                    "• Screenshots\n" +
                    "• Stored application package names"
        )

        Text(
            "Participation is voluntary. You can pause " +
                    "monitoring or withdraw consent at any time. " +
                    "Withdrawing consent removes your saved " +
                    "consent timestamp. Monitoring requires " +
                    "explicit activation in this application " +
                    "and accessibility permission in Android settings. " +
                    "This application does not provide a medical diagnosis."
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = consentChecked,
                onCheckedChange = { consentChecked = it },
                enabled = !isSaving
            )

            Text("I understand and agree to participate.")
        }

        Button(
            onClick = onAccept,
            enabled = consentChecked && !isSaving
        ) {
            Text(if (isSaving) "Saving..." else "Continue")
        }
    }
}



@Composable
fun HomeScreen(
    monitoringEnabled: Boolean,
    isSaving: Boolean,
    isWithdrawing: Boolean,
    onToggleMonitoring: () -> Unit,
    onWithdraw: () -> Unit
) {

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Text(
            text = "Burnout Detector",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                isWithdrawing -> "Withdrawing consent..."
                isSaving -> "Updating preferences..."
                monitoringEnabled -> "Monitoring requested"
                else -> "Monitoring paused"
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(

            onClick = onToggleMonitoring,

            enabled = !isSaving && !isWithdrawing

        ) {

            Text(
                when {

                    isSaving -> "Please wait..."

                    monitoringEnabled -> "Pause monitoring"

                    else -> "Enable monitoring"
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onWithdraw,
            enabled = !isWithdrawing
        ) {
            Text(
                if (isWithdrawing)
                    "Withdrawing consent..."
                else
                    "Withdraw consent"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        Text(
            text = "For your privacy, monitoring must be " +
                    "manually enabled again after the " +
                    "application process or device restarts.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Prototype only: no interaction data " +
                    "is being collected yet.",
            style = MaterialTheme.typography.bodySmall
        )

    }
}
