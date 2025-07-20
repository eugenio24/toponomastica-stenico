package com.ferrarieugenio.toponomastica_stenico_app.util.download

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(
    private val context: Context,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) {
    fun checkAndRequestPermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Permission not needed below Android 13
            onPermissionGranted()
            return
        }

        if (hasNotificationPermission()) {
            onPermissionGranted()
        } else {
            // callback launched in handlePermissionResult
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun handlePermissionResult(
        granted: Boolean,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (granted) {
            onGranted()
        } else {
            onDenied()
        }
    }
}
