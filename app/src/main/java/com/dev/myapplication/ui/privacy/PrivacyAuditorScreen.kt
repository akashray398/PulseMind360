package com.dev.myapplication.ui.privacy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.domain.privacy.DataInventoryItem
import com.dev.myapplication.domain.privacy.PermissionAuditItem
import com.dev.myapplication.domain.privacy.PrivacyRiskFinding
import com.dev.myapplication.domain.privacy.RiskCategory
import com.dev.myapplication.ui.components.GlassCard
import com.dev.myapplication.ui.theme.StatusError
import com.dev.myapplication.ui.theme.StatusGood
import com.dev.myapplication.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyAuditorScreen(
    onClose: () -> Unit,
    viewModel: PrivacyAuditorViewModel = hiltViewModel()
) {
    val report by viewModel.report.collectAsStateWithLifecycle()
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0=Permissions, 1=Inventory, 2=Risks, 3=Controls
    var showReportPreviewDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Privacy Command Center",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Permission Audit & Data Inventory",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshReport() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Audit")
                    }
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Hero Score Banner
            report?.let { rep ->
                HeroPrivacyScoreBanner(score = rep.score, riskCount = rep.privacyRisks.size)
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Permissions", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Inventory", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("Risks (${report?.privacyRisks?.size ?: 0})", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = activeTab == 3, onClick = { activeTab = 3 }, text = { Text("Controls", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            }

            // Tab Content
            report?.let { rep ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (activeTab) {
                        0 -> PermissionsAuditList(items = rep.permissionSummary)
                        1 -> DataInventoryMatrixList(items = rep.dataInventory)
                        2 -> PrivacyRisksList(findings = rep.privacyRisks)
                        3 -> PrivacyControlsPanel(
                            prefs = prefs,
                            viewModel = viewModel,
                            onPreviewReport = { showReportPreviewDialog = true }
                        )
                    }
                }
            }
        }

        if (showReportPreviewDialog && report != null) {
            ReportPreviewDialog(
                report = report!!,
                onDismiss = { showReportPreviewDialog = false }
            )
        }
    }
}

@Composable
fun HeroPrivacyScoreBanner(score: Int, riskCount: Int) {
    val (color, text) = when {
        score >= 85 -> StatusGood to "EXCELLENT PRIVACY SCORE"
        score >= 65 -> StatusWarning to "STABLE - ATTENTION SUGGESTED"
        else -> StatusError to "PRIVACY RISKS DETECTED"
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "$score% · $text", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
                    Text(text = "$riskCount verified risk finding(s) evaluated", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PermissionsAuditList(items: List<PermissionAuditItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.permissionName }) { item ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.permissionName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.isGranted) StatusGood.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (item.isGranted) "GRANTED" else "DENIED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isGranted) StatusGood else StatusWarning,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(text = "Reason: ${item.reasonRequired}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Feature: ${item.relatedFeature}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Action: ${item.recommendedAction}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun DataInventoryMatrixList(items: List<DataInventoryItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.category }) { item ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                text = item.storageLocation,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(text = "Purpose: ${item.purpose}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Collection: ${item.collectionMethod} · Retention: ${item.retentionPeriod}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PrivacyRisksList(findings: List<PrivacyRiskFinding>) {
    if (findings.isEmpty()) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusGood)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "No Verified Privacy Risks Detected!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(findings) { finding ->
                val color = when (finding.severity) {
                    RiskCategory.HIGH -> StatusError
                    RiskCategory.MEDIUM -> StatusWarning
                    RiskCategory.LOW -> MaterialTheme.colorScheme.primary
                }

                GlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = finding.severity == RiskCategory.HIGH) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Risk Severity: ${finding.severity.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
                            Text(text = "Component: ${finding.affectedComponent}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(text = finding.explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = "Evidence: ${finding.evidence}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Remediation: ${finding.recommendedRemediation}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyControlsPanel(
    prefs: com.dev.myapplication.data.repository.UserPreferences,
    viewModel: PrivacyAuditorViewModel,
    onPreviewReport: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Telemetry & Transmission Controls", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Battery Telemetry Collection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Observes battery level %, voltage, and temperature", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = prefs.collectBatteryTelemetry, onCheckedChange = { viewModel.setCollectBatteryTelemetry(it) })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Network Traffic Telemetry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Monitors downstream/upstream link bandwidth", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = prefs.collectNetworkTelemetry, onCheckedChange = { viewModel.setCollectNetworkTelemetry(it) })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Redact PDF Export Metadata", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Redacts public IP address by default during PDF exports", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = prefs.redactExportMetadata, onCheckedChange = { viewModel.setRedactExportMetadata(it) })
                }
            }
        }

        Button(
            onClick = onPreviewReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Preview Redacted Privacy Audit Report")
        }
    }
}

@Composable
fun ReportPreviewDialog(
    report: com.dev.myapplication.domain.privacy.PrivacyAuditReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Redacted Privacy Audit Report", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Audit Score: ${report.score}% · Timestamp: ${report.auditTimestamp}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "Permissions Audited: ${report.permissionSummary.size} items", fontSize = 11.sp)
                Text(text = "Data Categories Audited: ${report.dataInventory.size} items", fontSize = 11.sp)
                Text(text = "Verified Privacy Risks: ${report.privacyRisks.size} findings", fontSize = 11.sp, color = StatusWarning)

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Text(text = "Redaction Notice:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Public IP and hardware IDs are redacted by default (192.168.xxx.xxx).", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close Preview") }
        }
    )
}
