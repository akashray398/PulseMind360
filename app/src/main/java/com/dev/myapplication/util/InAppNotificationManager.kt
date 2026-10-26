package com.dev.myapplication.util

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InAppNotificationData(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val type: String = "INFO", // INFO, WARNING, ALERT, SUCCESS
    val icon: ImageVector? = null,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

object InAppNotificationManager {
    private val _currentNotification = MutableStateFlow<InAppNotificationData?>(null)
    val currentNotification: StateFlow<InAppNotificationData?> = _currentNotification.asStateFlow()

    fun show(notification: InAppNotificationData) {
        _currentNotification.value = notification
    }

    fun dismiss() {
        _currentNotification.value = null
    }
}
