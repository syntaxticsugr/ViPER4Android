package com.llsl.viper4android.ui.screens.device

import android.text.format.DateUtils
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.llsl.viper4android.R
import com.llsl.viper4android.data.model.DeviceSettings
import com.llsl.viper4android.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDialog(
    devices: List<DeviceSettings>,
    activeDeviceId: String,
    onRename: (String, String) -> Unit,
    onLoad: (String) -> Unit,
    onSave: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDeviceId by remember { mutableStateOf<String?>(null) }
    var renamingDeviceId by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }

    val selectedDevice = selectedDeviceId?.let { id -> devices.find { it.deviceId == id } }

    if (renamingDeviceId != null) {
        AlertDialog(
            onDismissRequest = { renamingDeviceId = null },
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text(stringResource(R.string.device_rename_title)) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text(stringResource(R.string.device_rename_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRename(renamingDeviceId!!, renameInput.trim())
                            renamingDeviceId = null
                        }
                    },
                    enabled = renameInput.isNotBlank(),
                ) {
                    Text(stringResource(R.string.action_rename))
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingDeviceId = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
        return
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(vertical = Dimens.dialogPadding),
            ) {
                // Header: a back arrow plus rename when a device is selected, otherwise the list title.
                if (selectedDevice != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = Dimens.space, end = Dimens.space, bottom = Dimens.spaceMd),
                    ) {
                        IconButton(onClick = { selectedDeviceId = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                        Text(
                            text = selectedDevice.deviceName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(onClick = {
                            renameInput = selectedDevice.deviceName
                            renamingDeviceId = selectedDevice.deviceId
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.action_rename),
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.device_dialog_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier.padding(
                                start = Dimens.dialogPadding,
                                end = Dimens.dialogPadding,
                                bottom = Dimens.spaceMd,
                            ),
                    )
                }

                // Detail view for the chosen device, or the full list when nothing is selected.
                if (selectedDevice != null) {
                    DeviceDetailView(
                        device = selectedDevice,
                        isActive = selectedDevice.deviceId == activeDeviceId,
                        onLoad = { onLoad(selectedDevice.deviceId) },
                        onSave = { onSave(selectedDevice.deviceId) },
                        onDelete = {
                            onDelete(selectedDevice.deviceId)
                            selectedDeviceId = null
                        },
                    )
                } else {
                    DeviceListView(
                        devices = devices,
                        activeDeviceId = activeDeviceId,
                        onSelect = { selectedDeviceId = it.deviceId },
                    )
                }

                // Close button, shown only on the list view; the detail view has its own actions.
                if (selectedDevice == null) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.spaceMd, end = Dimens.space),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.action_close))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceListView(
    devices: List<DeviceSettings>,
    activeDeviceId: String,
    onSelect: (DeviceSettings) -> Unit,
) {
    if (devices.isEmpty()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spaceXl),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.device_no_devices),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val sorted =
        remember(devices, activeDeviceId) {
            devices.sortedWith(
                compareByDescending<DeviceSettings> { it.deviceId == activeDeviceId }
                    .thenByDescending { it.lastConnected },
            )
        }

    LazyColumn(
        modifier = Modifier.heightIn(max = 360.dp),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(horizontal = Dimens.dialogPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
    ) {
        items(sorted, key = { it.deviceId }) { device ->
            val isActive = device.deviceId == activeDeviceId
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(device) }
                            .padding(horizontal = Dimens.space, vertical = Dimens.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = deviceIcon(device),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.rowIconSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(Dimens.rowIconGap))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isActive) {
                                Canvas(modifier = Modifier.size(8.dp)) {
                                    drawCircle(Color(0xFF4CAF50))
                                }
                                Spacer(modifier = Modifier.width(Dimens.spaceXs))
                            }
                            Text(
                                text = device.deviceName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (!isActive) {
                            Text(
                                text =
                                    DateUtils
                                        .getRelativeTimeSpanString(
                                            device.lastConnected,
                                            System.currentTimeMillis(),
                                            DateUtils.MINUTE_IN_MILLIS,
                                        ).toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val BUILTIN_DEVICE_IDS = setOf("speaker", "wired_headphone")

@Composable
private fun DeviceDetailView(
    device: DeviceSettings,
    isActive: Boolean,
    onLoad: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    val isBuiltIn = device.deviceId in BUILTIN_DEVICE_IDS
    val canDelete = !isActive && !isBuiltIn
    Column(modifier = Modifier.padding(horizontal = Dimens.dialogPadding)) {
        StatusRow(
            label = stringResource(R.string.device_label_type),
            value = deviceTypeName(device),
        )
        StatusRow(
            label = stringResource(R.string.device_label_address),
            value = if (device.deviceId == "speaker" || device.deviceId == "wired_headphone") "-" else device.deviceId,
        )
        StatusRow(
            label = stringResource(R.string.label_mode),
            value =
                if (device.isHeadphone) {
                    stringResource(R.string.device_mode_headphone)
                } else {
                    stringResource(R.string.device_mode_speaker)
                },
        )
        StatusRow(
            label = stringResource(R.string.device_label_last_conn),
            value =
                if (isActive) {
                    "-"
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Date(device.lastConnected))
                },
        )
        Spacer(modifier = Modifier.height(Dimens.space))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ActionItem(
                icon = Icons.Default.SettingsBackupRestore,
                label = stringResource(R.string.action_load),
                onClick = onLoad,
            )
            ActionItem(
                icon = Icons.Default.Sync,
                label = stringResource(R.string.action_update),
                onClick = onSave,
            )
            ActionItem(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.action_delete),
                onClick = onDelete,
                enabled = canDelete,
                tint =
                    if (!canDelete) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.spaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .clickable(enabled = enabled) { onClick() }
                .padding(Dimens.spaceSm),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(Dimens.rowIconSize), tint = tint)
        Spacer(modifier = Modifier.height(Dimens.spaceXs))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

private fun deviceIcon(device: DeviceSettings) =
    when {
        device.isHeadphone -> Icons.Default.Headphones
        else -> Icons.Default.Speaker
    }

@Composable
private fun deviceTypeName(device: DeviceSettings): String =
    when {
        device.deviceId == "speaker" -> stringResource(R.string.device_type_speaker)
        device.deviceId == "wired_headphone" -> stringResource(R.string.device_type_wired)
        device.isHeadphone -> stringResource(R.string.device_type_bluetooth)
        else -> stringResource(R.string.device_type_speaker)
    }
