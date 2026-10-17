package com.dev.myapplication.ui.automation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: AutomationViewModel = hiltViewModel()
) {
    val rules by viewModel.rulesState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                    Text(
                        text = "AUTOMATION ENGINE",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "User-Configurable Contextual Rules",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(onClick = { viewModel.testRunRules() }) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Test Rules", modifier = Modifier.padding(end = 4.dp))
                    Text(text = "Run Engine", fontSize = 12.sp)
                }
            }

            if (rules.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp).fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "No rules",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "No Automation Rules Created",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+' to create contextual battery/network rules.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rules, key = { it.id }) { rule ->
                        AutomationRuleItem(
                            rule = rule,
                            onToggle = { viewModel.toggleRule(rule) },
                            onDelete = { viewModel.deleteRule(rule) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rule")
        }
    }

    if (showAddDialog) {
        CreateRuleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, condType, condVal, actType, actTarget ->
                viewModel.addRule(name, condType, condVal, actType, actTarget)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AutomationRuleItem(
    rule: AutomationRuleEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = rule.isEnabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = rule.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "IF: ${rule.conditionType} (${rule.conditionValue})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "THEN: ${rule.actionType} ${if (rule.actionTarget.isNotBlank()) "(${rule.actionTarget})" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (rule.lastTriggeredAt > 0) {
                    Text(
                        text = "Last triggered: ${dateFormat.format(Date(rule.lastTriggeredAt))}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Rule",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRuleDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("BATTERY_BELOW") }
    var conditionValue by remember { mutableIntStateOf(20) }
    var selectedAction by remember { mutableStateOf("SHOW_NOTIFICATION") }
    var actionTarget by remember { mutableStateOf("") }

    val conditions = listOf("BATTERY_BELOW", "CHARGING_STATE_CHANGED", "NETWORK_DISCONNECTED", "NETWORK_CONNECTED")
    val actions = listOf("SHOW_NOTIFICATION", "LOG_EVENT", "TRIGGER_SYNC_WORKER")

    var condExpanded by remember { mutableStateOf(false) }
    var actExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Automation Rule", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Condition Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = condExpanded,
                    onExpandedChange = { condExpanded = !condExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCondition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Condition") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = condExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = condExpanded,
                        onDismissRequest = { condExpanded = false }
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond) },
                                onClick = {
                                    selectedCondition = cond
                                    condExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedCondition == "BATTERY_BELOW") {
                    OutlinedTextField(
                        value = conditionValue.toString(),
                        onValueChange = { conditionValue = it.toIntOrNull() ?: 20 },
                        label = { Text("Battery Threshold (%)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Action Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = actExpanded,
                    onExpandedChange = { actExpanded = !actExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAction,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Action") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = actExpanded,
                        onDismissRequest = { actExpanded = false }
                    ) {
                        actions.forEach { act ->
                            DropdownMenuItem(
                                text = { Text(act) },
                                onClick = {
                                    selectedAction = act
                                    actExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedAction == "SHOW_NOTIFICATION") {
                    OutlinedTextField(
                        value = actionTarget,
                        onValueChange = { actionTarget = it },
                        label = { Text("Notification Message") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(name, selectedCondition, conditionValue, selectedAction, actionTarget)
                }
            ) {
                Text("Create Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
