package com.llsl.viper4android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.llsl.viper4android.ui.theme.Dimens

/**
 * One effect row in a segmented card group. The surface takes its corner shape and the gap below
 * it from [LocalCardGroupPosition], so a run of adjacent sections reads as a single rounded panel
 * split by thin seams. The row shows a leading [icon], a [title] (with an optional [subtitle] that
 * summarises the current value), and a trailing [Switch].
 *
 * Tapping the row expands [content] inline with an animation, unless [toggleOnly] is set — those
 * rows just flip the switch and never expand (think "Master limiter"). Set [hasEnableSwitch] to
 * false to drop the trailing switch entirely, for rows that are purely informational. If
 * [descriptionRes] is supplied, a long-press opens a detail sheet explaining the effect.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EffectSection(
    title: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    hasEnableSwitch: Boolean = true,
    toggleOnly: Boolean = false,
    initiallyExpanded: Boolean = false,
    subtitle: String? = null,
    descriptionRes: Int? = null,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    var showDetails by rememberSaveable { mutableStateOf(false) }
    val position = LocalCardGroupPosition.current

    Surface(
        modifier = modifier.fillMaxWidth().padding(bottom = position.bottomGap()),
        shape = position.shape(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            val hasDetails = descriptionRes != null
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (toggleOnly && !hasDetails) {
                                Modifier
                            } else {
                                Modifier.combinedClickable(
                                    onClick = { if (!toggleOnly) expanded = !expanded },
                                    onLongClick = if (hasDetails) ({ showDetails = true }) else null,
                                )
                            },
                        )
                        .heightIn(min = Dimens.rowMinHeight)
                        .padding(horizontal = Dimens.rowPaddingH, vertical = Dimens.rowPaddingV),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.rowIconSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(Dimens.rowIconGap))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.size(Dimens.titleSummaryGap))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (hasEnableSwitch) {
                    Spacer(modifier = Modifier.width(Dimens.spaceMd))
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChange,
                    )
                }
            }

            if (!toggleOnly) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = Dimens.rowPaddingH,
                                    end = Dimens.rowPaddingH,
                                    bottom = Dimens.space,
                                ),
                    ) {
                        content()
                    }
                }
            }
        }
    }

    if (showDetails && descriptionRes != null) {
        EffectDetailSheet(
            title = title,
            descriptionHtml = stringResource(descriptionRes),
            onDismiss = { showDetails = false },
        )
    }
}
