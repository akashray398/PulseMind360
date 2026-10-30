package com.dev.myapplication.ui.automation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.ui.components.GlassCard
import com.dev.myapplication.ui.theme.StatusError
import com.dev.myapplication.ui.theme.StatusGood
import com.dev.myapplication.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: AutomationViewModel = hiltViewModel()
) {
    val rules by viewModel.rulesState.collectAsStateWithLifecycle()
    val feedback by viewModel.evaluationFeedback.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // ALL, BATTERY, NETWORK

    val filteredRules = remember(rules, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "BATTERY" -> rules.filter { it.conditionType.contains("BATTERY") || it.conditionType.contains("CHARGING") }
            "NETWORK" -> rules.filter { it.conditionType.contains("NETWORK") }
            else -> rules
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
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
                        text = "Contextual Intelligence Rules & Logic Pipeline",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(onClick = { viewModel.testRunRules() }) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Test Rules", modifier = Modifier.padding(end = 4.dp))
                    Text(text = "Run Engine", fontSize = 12.sp)
                }
            }

            // Live Engine Evaluation Feedback Banner
            feedback?.let { msg ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hasGlowBorder = true
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Feedback",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearFeedback() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // One-Tap Smart Automation Recipes Bar
            Text(
                text = "1-Click Smart Automation Recipes",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    RecipeChip(
                        title = "Low Battery Saver",
                        subtitle = "HIGH PRIORITY · IF Battery <= 15%",
                        icon = Icons.Default.BatteryAlert,
                        onClick = { viewModel.addPresetRecipe("LOW_BATTERY") }
                    )
                }
                item {
                    RecipeChip(
                        title = "Wi-Fi Sync Guard",
                        subtitle = "MED PRIORITY · IF Wi-Fi -> Sync",
                        icon = Icons.Default.Wifi,
                        onClick = { viewModel.addPresetRecipe("WIFI_SYNC") }
                    )
                }
                item {
                    RecipeChip(
                        title = "Charging Alert",
                        subtitle = "MED PRIORITY · IF Plugged In",
                        icon = Icons.Default.Bolt,
                        onClick = { viewModel.addPresetRecipe("CHARGING_ALERT") }
                    )
                }
                item {
                    RecipeChip(
                        title = "Offline Event Log",
                        subtitle = "LOW PRIORITY · IF Disconnected",
                        icon = Icons.Default.SignalCellularConnectedNoInternet4Bar,
                        onClick = { viewModel.addPresetRecipe("OFFLINE_LOG") }
                    )
                }
            }

            // Category Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "All Rules", "BATTERY" to "Battery Rules", "NETWORK" to "Network Rules").forEach { (type, label) ->
                    FilterChip(
                        selected = selectedCategoryFilter == type,
                        onClick = { selectedCategoryFilter = type },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            if (filteredRules.isEmpty()) {
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
                            text = "No Automation Rules Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap a 1-Click Recipe above or '+' to create a rule.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRules, key = { it.id }) { rule ->
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
            onSave = { name, condType, condVal, secCondType, secCondVal, op, actType, actTarget, cooldown, prio ->
                viewModel.addRule(
                    name = name,
                    conditionType = condType,
                    conditionValue = condVal,
                    secondaryConditionType = secCondType,
                    secondaryConditionValue = secCondVal,
                    logicalOperator = op,
                    actionType = actType,
                    actionTarget = actTarget,
                    cooldownMinutes = cooldown,
                    priority = prio
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RecipeChip(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AutomationRuleItem(
    rule: AutomationRuleEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    val (prioColor, prioText) = when (rule.priority) {
        1 -> StatusError to "HIGH PRIORITY"
        2 -> StatusWarning to "MED PRIORITY"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to "LOW PRIORITY"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = rule.isEnabled
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (rule.isEnabled) StatusGood else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rule.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = prioColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, prioColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = prioText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = prioColor,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val condText = if (rule.logicalOperator != "NONE" && rule.secondaryConditionType.isNotBlank()) {
                    "IF: ${rule.conditionType} (${rule.conditionValue}) ${rule.logicalOperator} ${rule.secondaryConditionType}"
                } else {
                    "IF: ${rule.conditionType} (${rule.conditionValue})"
                }

                Text(
                    text = condText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "THEN: ${rule.actionType} ${if (rule.actionTarget.isNotBlank()) "(${rule.actionTarget})" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Explanation & Reason Line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Reason",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rule.lastExecutionReason,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "⚡ Triggered ${rule.triggerCount} times · Cooldown: ${rule.cooldownMinutes}m",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (rule.lastTriggeredAt > 0) {
                        Text(
                            text = "Last: ${dateFormat.format(Date(rule.lastTriggeredAt))}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { onToggle() }
                )

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRuleDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, String, Int, String, String, String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("BATTERY_BELOW") }
    var conditionValue by remember { mutableIntStateOf(20) }
    var logicalOperator by remember { mutableStateOf("NONE") } // NONE, AND, OR
    var secondaryCondition by remember { mutableStateOf("") }
    var secondaryValue by remember { mutableIntStateOf(0) }
    var selectedAction by remember { mutableStateOf("SHOW_NOTIFICATION") }
    var actionTarget by remember { mutableStateOf("") }
    var cooldownMinutes by remember { mutableIntStateOf(5) }
    var priority by remember { mutableIntStateOf(2) }

    val conditions = listOf("BATTERY_BELOW", "CHARGING_STATE_CHANGED", "NETWORK_DISCONNECTED", "NETWORK_CONNECTED")
    val actions = listOf("SHOW_NOTIFICATION", "LOG_EVENT", "TRIGGER_SYNC_WORKER")
    val operators = listOf("NONE", "AND", "OR")

    var condExpanded by remember { mutableStateOf(false) }
    var opExpanded by remember { mutableStateOf(false) }
    var secCondExpanded by remember { mutableStateOf(false) }
    var actExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Automation Intelligence Rule", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Primary Condition Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = condExpanded,
                    onExpandedChange = { condExpanded = !condExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCondition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primary Condition") },
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

                // Logical Operator Dropdown
                ExposedDropdownMenuBox(
                    expanded = opExpanded,
                    onExpandedChange = { opExpanded = !opExpanded }
                ) {
                    OutlinedTextField(
                        value = logicalOperator,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Logical Operator") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = opExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = opExpanded,
                        onDismissRequest = { opExpanded = false }
                    ) {
                        operators.forEach { op ->
                            DropdownMenuItem(
                                text = { Text(op) },
                                onClick = {
                                    logicalOperator = op
                                    opExpanded = false
                                }
                            )
                        }
                    }
                }

                if (logicalOperator != "NONE") {
                    ExposedDropdownMenuBox(
                        expanded = secCondExpanded,
                        onExpandedChange = { secCondExpanded = !secCondExpanded }
                    ) {
                        OutlinedTextField(
                            value = secondaryCondition.ifBlank { "Select Secondary Condition" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Secondary Condition") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = secCondExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = secCondExpanded,
                            onDismissRequest = { secCondExpanded = false }
                        ) {
                            conditions.forEach { cond ->
                                DropdownMenuItem(
                                    text = { Text(cond) },
                                    onClick = {
                                        secondaryCondition = cond
                                        secCondExpanded = false
                                    }
                                )
                            }
                        }
                    }
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = cooldownMinutes.toString(),
                        onValueChange = { cooldownMinutes = it.toIntOrNull() ?: 5 },
                        label = { Text("Cooldown (mins)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = priority.toString(),
                        onValueChange = { priority = (it.toIntOrNull() ?: 2).coerceIn(1, 3) },
                        label = { Text("Priority (1=High)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        name,
                        selectedCondition,
                        conditionValue,
                        secondaryCondition,
                        secondaryValue,
                        logicalOperator,
                        selectedAction,
                        actionTarget,
                        cooldownMinutes,
                        priority
                    )
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
