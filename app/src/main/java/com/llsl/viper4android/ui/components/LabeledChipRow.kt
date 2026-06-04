package com.llsl.viper4android.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.llsl.viper4android.ui.theme.Dimens

/**
 * A horizontally scrollable row of selectable chips — one chip per entry in [options], with the
 * [selectedIndex] highlighted. Handy when the choices are few and worth showing at a glance (e.g.
 * the equalizer's band count) rather than hiding behind a picker. Pass a [label] to caption the
 * row; leave it null when the surrounding row already makes the choice obvious.
 */
@Composable
fun LabeledChipRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = Dimens.spaceSm)) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.size(Dimens.spaceSm))
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
        ) {
            options.forEachIndexed { index, option ->
                PresetChip(
                    label = option,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}
