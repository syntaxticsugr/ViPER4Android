package com.llsl.viper4android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.llsl.viper4android.ui.theme.Dimens

/**
 * A single-choice picker dialog with an optional centered [icon] and [title], a scrollable column
 * of [options], and an optional [hint] line. Two looks are supported: by default the chosen option
 * is a filled primaryContainer pill (used for short "mode" lists like Clarity mode); set [radio] to
 * draw classic radio circles instead, the way the file pickers (Headset correction, Convolver
 * kernel) present their longer lists. When [onLongPressOption] is supplied, long-pressing an option
 * calls it — used to delete an imported file.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OptionPickerDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    hint: String? = null,
    radio: Boolean = false,
    onLongPressOption: ((Int) -> Unit)? = null,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Dimens.dialogCorner),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(vertical = Dimens.dialogPadding),
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = Dimens.space),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = Dimens.dialogPadding, end = Dimens.dialogPadding, bottom = Dimens.spaceLg),
                )

                Column(
                    modifier =
                        Modifier
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    options.forEachIndexed { index, option ->
                        val selected = index == selectedIndex
                        val selectModifier =
                            if (onLongPressOption != null) {
                                Modifier.combinedClickable(
                                    onClick = { onSelect(index) },
                                    onLongClick = { onLongPressOption(index) },
                                )
                            } else {
                                Modifier.selectable(
                                    selected = selected,
                                    role = Role.RadioButton,
                                    onClick = { onSelect(index) },
                                )
                            }

                        if (radio) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .then(selectModifier)
                                        .heightIn(min = 52.dp)
                                        .padding(horizontal = Dimens.dialogPadding, vertical = Dimens.spaceSm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected, onClick = null)
                                Spacer(modifier = Modifier.width(Dimens.space))
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        } else {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimens.spaceMd, vertical = 1.dp)
                                        .clip(RoundedCornerShape(Dimens.singleChoiceCorner))
                                        .background(
                                            if (selected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                Color.Transparent
                                            },
                                        )
                                        .then(selectModifier)
                                        .heightIn(min = 52.dp)
                                        .padding(horizontal = Dimens.spaceLg, vertical = Dimens.spaceMd),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color =
                                        if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                            }
                        }
                    }
                }

                if (hint != null) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = Dimens.dialogPadding,
                                    end = Dimens.dialogPadding,
                                    top = Dimens.spaceMd,
                                ),
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.spaceSm, end = Dimens.space),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            }
        }
    }
}
