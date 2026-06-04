package com.llsl.viper4android.ui.screens.preset

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Restore
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.llsl.viper4android.R
import com.llsl.viper4android.data.model.Preset
import com.llsl.viper4android.ui.theme.Dimens

/**
 * The Presets dialog: a rounded surface listing saved presets as filled pill rows, each with a
 * leading file icon. Tap a row to load it, long-press to rename, and use the trailing button to
 * delete (with an inline undo). A "New" action prompts for a name to save the current settings,
 * and "Cancel" closes the dialog. The pill styling mirrors the reference app's lavender preset list.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresetDialog(
    presets: List<Preset>,
    onSave: (String) -> Unit,
    onLoad: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var showSaveInput by remember { mutableStateOf(false) }
    var saveInputName by remember { mutableStateOf("") }
    var renamingId by remember { mutableLongStateOf(-1L) }
    var renameInputName by remember { mutableStateOf("") }
    var pendingDeletePreset by remember { mutableStateOf<Preset?>(null) }

    fun commitPendingDelete() {
        pendingDeletePreset?.let { onDelete(it.id) }
        pendingDeletePreset = null
    }

    val visiblePresets =
        remember(presets, pendingDeletePreset) {
            if (pendingDeletePreset != null) {
                presets.filter { it.id != pendingDeletePreset!!.id }
            } else {
                presets
            }
        }

    if (showSaveInput) {
        AlertDialog(
            onDismissRequest = { showSaveInput = false },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = { Text(stringResource(R.string.preset_save_title)) },
            text = {
                OutlinedTextField(
                    value = saveInputName,
                    onValueChange = { saveInputName = it },
                    label = { Text(stringResource(R.string.preset_name_hint)) },
                    singleLine = true,
                    isError = saveInputName.isBlank(),
                    trailingIcon = {
                        if (saveInputName.isBlank()) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    supportingText = {
                        if (saveInputName.isBlank()) {
                            Text(stringResource(R.string.preset_name_required))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (saveInputName.isNotBlank()) {
                            onSave(saveInputName.trim())
                            saveInputName = ""
                            showSaveInput = false
                        }
                    },
                    enabled = saveInputName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveInput = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
        return
    }

    if (renamingId >= 0) {
        AlertDialog(
            onDismissRequest = { renamingId = -1L },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = { Text(stringResource(R.string.preset_rename_title)) },
            text = {
                OutlinedTextField(
                    value = renameInputName,
                    onValueChange = { renameInputName = it },
                    label = { Text(stringResource(R.string.preset_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInputName.isNotBlank()) {
                            onRename(renamingId, renameInputName.trim())
                            renamingId = -1L
                        }
                    },
                    enabled = renameInputName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.action_rename))
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingId = -1L }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
        return
    }

    BasicAlertDialog(
        onDismissRequest = {
            commitPendingDelete()
            onDismiss()
        },
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(vertical = Dimens.dialogPadding),
            ) {
                Text(
                    text = stringResource(R.string.presets_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier.padding(
                            start = Dimens.dialogPadding,
                            end = Dimens.dialogPadding,
                            bottom = Dimens.spaceMd,
                        ),
                )

                if (visiblePresets.isEmpty() && pendingDeletePreset == null) {
                    Text(
                        text = stringResource(R.string.preset_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier.padding(
                                horizontal = Dimens.dialogPadding,
                                vertical = Dimens.spaceMd,
                            ),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        contentPadding =
                            androidx.compose.foundation.layout.PaddingValues(
                                horizontal = Dimens.dialogPadding,
                            ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    ) {
                        items(visiblePresets, key = { it.id }) { preset ->
                            PresetPill(
                                preset = preset,
                                onLoad = {
                                    commitPendingDelete()
                                    onLoad(preset.id)
                                },
                                onRename = {
                                    renameInputName = preset.name
                                    renamingId = preset.id
                                },
                                onDelete = {
                                    commitPendingDelete()
                                    pendingDeletePreset = preset
                                },
                            )
                        }
                        pendingDeletePreset?.let { deleted ->
                            item(key = "deleted_${deleted.id}") {
                                DeletedPresetPill(
                                    preset = deleted,
                                    onRestore = { pendingDeletePreset = null },
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = Dimens.space,
                                end = Dimens.space,
                                top = Dimens.spaceMd,
                            ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = {
                        showSaveInput = true
                        saveInputName = ""
                    }) {
                        Text(stringResource(R.string.preset_new))
                    }
                    TextButton(onClick = {
                        commitPendingDelete()
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }
}

/**
 * One saved preset, drawn as a filled pill: leading file icon, the preset name, and a Headphone /
 * Speaker subtitle. Tap loads it, long-press renames, the trailing button deletes. Painted on the
 * primaryContainer color to get the filled lavender look of the reference app's preset pills.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetPill(
    preset: Preset,
    onLoad: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = onLoad, onLongClick = onRename)
                    .padding(horizontal = Dimens.space, vertical = Dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(Dimens.rowIconSize),
            )
            Spacer(modifier = Modifier.width(Dimens.rowIconGap))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(if (preset.fxType == 1) R.string.tab_headphone else R.string.tab_speaker),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun DeletedPresetPill(
    preset: Preset,
    onRestore: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.space, vertical = Dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(Dimens.rowIconSize), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
            Spacer(modifier = Modifier.width(Dimens.rowIconGap))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Text(
                    text = stringResource(R.string.label_deleted),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                )
            }
            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
