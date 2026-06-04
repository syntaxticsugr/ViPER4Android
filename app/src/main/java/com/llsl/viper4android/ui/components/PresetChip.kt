package com.llsl.viper4android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.llsl.viper4android.ui.theme.Dimens

/**
 * A filled pill chip for selectable presets, such as the Flat / Deep / Bass booster choices in
 * the EQ editor. When [selected] it uses the primaryContainer tone to stand out; otherwise it
 * sits on the quieter surfaceContainerHigh.
 */
@Composable
fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container =
        if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    val content =
        if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        modifier = modifier.heightIn(min = 36.dp),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = Dimens.space, vertical = Dimens.spaceSm),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
