package com.dev.myapplication.ui.privacy

import androidx.compose.runtime.Composable

@Composable
fun PrivacyDashboardScreen(
    onClose: () -> Unit
) {
    PrivacyAuditorScreen(onClose = onClose)
}
