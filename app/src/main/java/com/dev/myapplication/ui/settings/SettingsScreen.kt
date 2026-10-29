package com.dev.myapplication.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.ui.components.GlassCard
import com.dev.myapplication.util.BiometricHelper
import com.dev.myapplication.util.KeystoreVaultManager

@Composable
fun SettingsScreen(
    onOpenPrivacyDashboard: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.userPreferencesState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }

    var hasNotificationPerm by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPerm = isGranted
        viewModel.setNotificationsEnabled(isGranted)
    }

    var showPurgeDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "SETTINGS & PRIVACY",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "App Theme, Alerts, Background Sync & Privacy Controls",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Theme Preference Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness4,
                    contentDescription = "Theme",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Application Theme",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                    FilterChip(
                        selected = prefs.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(mode, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Biometric Security Vault Card
        var vaultTestResult by remember { mutableStateOf<String?>(null) }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric Vault",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Biometric Security Vault", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = if (prefs.biometricVaultEnabled) "VAULT LOCKED (AES-256 KeyStore Protected)" else if (biometricHelper.isBiometricAvailable()) "Biometric Hardware Ready" else "Biometric Hardware Not Enrolled",
                        fontSize = 12.sp,
                        color = if (prefs.biometricVaultEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (prefs.biometricVaultEnabled) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Switch(
                    checked = prefs.biometricVaultEnabled,
                    enabled = biometricHelper.isBiometricAvailable(),
                    onCheckedChange = { checked ->
                        if (context is FragmentActivity) {
                            biometricHelper.promptBiometricAuth(
                                activity = context,
                                title = if (checked) "Enable Biometric Security Vault" else "Disable Biometric Security Vault",
                                subtitle = "Confirm fingerprint/PIN authentication",
                                onSuccess = { viewModel.setBiometricVaultEnabled(checked) },
                                onError = { viewModel.setBiometricVaultEnabled(!checked) }
                            )
                        } else {
                            viewModel.setBiometricVaultEnabled(checked)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    fun executeTest() {
                        val sampleSecret = "PulseMind_Vault_Secret_${System.currentTimeMillis()}"
                        val payload = KeystoreVaultManager.encrypt(sampleSecret)
                        val decrypted = KeystoreVaultManager.decrypt(payload)
                        vaultTestResult = if (decrypted == sampleSecret) {
                            "AES-256 KeyStore Verified! Cipher: ${payload.encryptedBase64.take(16)}..."
                        } else {
                            "KeyStore Encryption Failed"
                        }
                    }

                    if (prefs.biometricVaultEnabled && context is FragmentActivity) {
                        biometricHelper.promptBiometricAuth(
                            activity = context,
                            title = "Authorize KeyStore Vault Test",
                            subtitle = "Scan fingerprint/PIN to test AES-256 hardware vault",
                            onSuccess = { executeTest() },
                            onError = { vaultTestResult = "Authentication failed: KeyStore access denied." }
                        )
                    } else {
                        executeTest()
                    }
                },
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = "KeyStore Test")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test KeyStore Encryption", fontSize = 12.sp)
            }

            vaultTestResult?.let { result ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = result, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        // Low Battery Alert Threshold Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Low Battery Alert Threshold: ${prefs.lowBatteryThreshold}%",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = prefs.lowBatteryThreshold.toFloat(),
                onValueChange = { viewModel.setLowBatteryThreshold(it.toInt()) },
                valueRange = 5f..35f,
                steps = 5
            )
        }

        // 80% Smart Charge Lifespan Guard Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Smart Charge Guard",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "80% Smart Charge Guard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Notifies when battery reaches 80% while charging to extend battery health lifespan.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = prefs.smartChargeGuardEnabled,
                    onCheckedChange = { viewModel.setSmartChargeGuardEnabled(it) }
                )
            }
        }

        // Overheat Thermal Alarm Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = "Thermal Alarm",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Overheat Thermal Alarm (40°C)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Triggers an immediate high-priority alert when battery temperature exceeds 40°C.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = prefs.overheatAlarmEnabled,
                    onCheckedChange = { viewModel.setOverheatAlarmEnabled(it) }
                )
            }
        }

        // Notification Permission Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "System Notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = if (hasNotificationPerm) "Notifications Granted" else "Permission Required",
                        fontSize = 12.sp,
                        color = if (hasNotificationPerm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPerm) {
                    Button(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                        Text("Grant", fontSize = 12.sp)
                    }
                } else {
                    Switch(
                        checked = prefs.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            }
        }

        // Background Sync Interval Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Interval",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Background Sync Interval",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15 to "15 min", 30 to "30 min", 60 to "60 min").forEach { (mins, label) ->
                    FilterChip(
                        selected = prefs.syncIntervalMinutes == mins,
                        onClick = { viewModel.setSyncIntervalMinutes(mins) },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Privacy Command Center & Transparency Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Privacy Command Center",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Privacy Command Center", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Data Inventory, Redaction & Permissions Matrix", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Report Metadata Redaction Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Sanitize PDF Audit Reports", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = "Redacts IP addresses and sensitive network metadata in exported PDFs.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = prefs.redactExportMetadata,
                    onCheckedChange = { checked ->
                        if (!checked && prefs.biometricVaultEnabled && context is FragmentActivity) {
                            biometricHelper.promptBiometricAuth(
                                activity = context,
                                title = "Un-redact Sensitive Metadata",
                                subtitle = "Confirm fingerprint/PIN to export un-redacted reports",
                                onSuccess = { viewModel.setRedactExportMetadata(false) },
                                onError = { viewModel.setRedactExportMetadata(true) }
                            )
                        } else {
                            viewModel.setRedactExportMetadata(checked)
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // App Permissions Transparency Matrix
            Text(text = "App Permissions Rationale", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            val permissionsList = listOf(
                "Notifications" to "Triggers high-priority 80% charge & thermal overheat alerts",
                "Network State" to "Monitors cellular/Wi-Fi connection state and throughput speeds",
                "Biometric Hardware" to "Restricts security vault access via fingerprint/PIN authentication",
                "Internet" to "Queries public IP & ISP location context on manual request"
            )

            permissionsList.forEach { (permName, rationale) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Text(text = "• $permName:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = rationale, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Local Data Footprint Inventory
            Text(
                text = "📁 Local Inventory: 100% Offline Room DB (~0.35 MB) · Zero Cloud Tracking",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Button(
                onClick = onOpenPrivacyDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = "Privacy Dashboard")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Full Privacy Dashboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Privacy & Data Purge Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Privacy",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Privacy & Data Retention", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "All data is stored offline-first in local Room database.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    if (prefs.biometricVaultEnabled && context is FragmentActivity) {
                        biometricHelper.promptBiometricAuth(
                            activity = context,
                            title = "Purge Vault Confirmation",
                            subtitle = "Authenticate fingerprint/PIN to purge local logs",
                            onSuccess = { showPurgeDialog = true },
                            onError = { }
                        )
                    } else {
                        showPurgeDialog = true
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Purge")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Purge Local History")
            }
        }
    }

    if (showPurgeDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeDialog = false },
            title = { Text("Purge All Stored History?") },
            text = { Text("This will permanently clear all activity history logs from offline database.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purgeAllData()
                        showPurgeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Purge")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
