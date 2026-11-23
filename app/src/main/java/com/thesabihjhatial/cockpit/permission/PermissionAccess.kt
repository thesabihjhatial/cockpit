package com.thesabihjhatial.cockpit.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle

class PermissionAccess(val granted: Boolean, val request: () -> Unit)

class Permissions(
    private val granted: Set<String>,
    val request: () -> Unit,
    private val requestFromTap: () -> Unit,
) {
    fun access(permission: String) = PermissionAccess(permission in granted, requestFromTap)
}

private fun Context.grantedPermissions(permissions: Array<out String>): Set<String> =
    permissions.filter { hasPermission(it) }.toSet()

private fun Context.hasPermission(permission: String): Boolean =
    (permission == Manifest.permission.BLUETOOTH_CONNECT && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ||
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    runCatching { startActivity(intent) }
}

@Composable
fun rememberPermissions(vararg permissions: String): Permissions {
    val context = LocalContext.current
    var blocked by remember { mutableStateOf(emptySet<String>()) }
    var granted by remember { mutableStateOf(context.grantedPermissions(permissions)) }
    var settingsOnBlock by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = context.grantedPermissions(permissions)
        val activity = context.findActivity()
        blocked = result.filterValues { !it }.keys
            .filter { permission -> activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) }
            .toSet()
        if (settingsOnBlock && blocked.isNotEmpty()) context.openAppSettings()
        settingsOnBlock = false
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { granted = context.grantedPermissions(permissions) }

    val request = {
        val missing = permissions.filter { it !in granted }
        when {
            missing.isEmpty() -> Unit
            missing.any { it in blocked } -> context.openAppSettings()
            else -> launcher.launch(missing.toTypedArray())
        }
    }

    return Permissions(granted, request) {
        settingsOnBlock = true
        request()
    }
}
