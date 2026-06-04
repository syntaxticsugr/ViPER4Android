package com.llsl.viper4android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.llsl.viper4android.R
import com.llsl.viper4android.ui.theme.Dimens

/**
 * A picker for an imported audio file — the DDC "Headset correction" and Convolver "Kernel"
 * selectors. It shows a tappable row with the [label] over the current [selectedValue], and opens
 * a radio-button dialog of the available [options] (with the dialog's [icon] above its title).
 * When [onDeleteOption] is given, long-pressing any option beyond the first lets the user delete
 * that file. This is deliberately a different look from the mode selectors: file lists can get
 * long, and radio circles read better there than a row of pills.
 */
@Composable
fun LabeledFilePicker(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onDeleteOption: ((Int, String) -> Unit)? = null,
) {
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Pair<Int, String>?>(null) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { showDialog = true }
                .padding(vertical = Dimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            if (selectedValue.isNotEmpty()) {
                Spacer(modifier = Modifier.width(Dimens.titleSummaryGap))
                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimens.spaceSm))
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (showDialog) {
        OptionPickerDialog(
            title = label,
            options = options,
            selectedIndex = options.indexOf(selectedValue).coerceAtLeast(0),
            onSelect = { index ->
                onOptionSelected(index, options[index])
                showDialog = false
            },
            onDismiss = { showDialog = false },
            icon = icon,
            radio = true,
            onLongPressOption =
                onDeleteOption?.let {
                    { index ->
                        if (index > 0) {
                            deleteTarget = index to options[index]
                            showDialog = false
                        } else {
                            onOptionSelected(index, options[index])
                            showDialog = false
                        }
                    }
                },
        )
    }

    deleteTarget?.let { (index, name) ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = { Text(stringResource(R.string.delete_file_title)) },
            text = { Text(stringResource(R.string.delete_file_message, name)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteOption?.invoke(index, name)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
