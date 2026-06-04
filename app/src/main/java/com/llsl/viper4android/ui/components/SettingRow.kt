package com.llsl.viper4android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llsl.viper4android.ui.theme.Dimens

/**
 * One card in the Settings list. Like the effect rows on the home screen, each setting is its own
 * card within a [SettingsCardGroup], separated from its neighbours by a thin seam. When a [content]
 * block is supplied the card becomes expandable: tapping it reveals [content] nested *inside* the
 * same card (no separating gap), and a chevron flips to show the state. Otherwise it's a plain row
 * with an optional leading [icon], a [title]/[subtitle], a [trailing] composable (often a Switch),
 * and an [onClick].
 */
@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    expanded: Boolean = false,
    content: (@Composable () -> Unit)? = null,
) {
    val position = LocalCardGroupPosition.current
    Surface(
        modifier = modifier.fillMaxWidth().padding(bottom = position.bottomGap()),
        shape = position.shape(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                        .heightIn(min = Dimens.rowMinHeight)
                        .padding(horizontal = Dimens.spaceLg, vertical = Dimens.spaceLg),
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
                    // 18sp keeps setting titles readable without dominating the row.
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
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
                if (content != null) {
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
                    Spacer(modifier = Modifier.width(Dimens.space))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.rotate(rotation),
                    )
                } else if (trailing != null) {
                    Spacer(modifier = Modifier.width(Dimens.space))
                    trailing()
                }
            }

            if (content != null) {
                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.spaceSm)) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * A child row shown nested inside an expanded [SettingRow] (e.g. the import actions under "Audio
 * processing files"). It carries no card of its own — it sits flush within the parent card, indented
 * a little past the leading [icon] so it reads as a sub-item.
 */
@Composable
fun SettingSubRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .heightIn(min = 52.dp)
                .padding(horizontal = Dimens.spaceLg, vertical = Dimens.spaceSm),
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Lays out [items] as one segmented card group — each item is a separate card tagged with its
 * position (top / middle / bottom / single) via [LocalCardGroupPosition], so the section reads as
 * a run of cards split by thin seams, just like the effect list. Place inside a vertical Column.
 */
@Composable
fun SettingsCardGroup(items: List<@Composable () -> Unit>) {
    items.forEachIndexed { index, item ->
        CompositionLocalProvider(
            LocalCardGroupPosition provides CardGroupPosition.of(index, items.size),
        ) {
            item()
        }
    }
}

/**
 * A category header above a [SettingsCardGroup], for example "Processing" or "ViPER4Android".
 * A neutral onSurfaceVariant label rather than the accent colour, so it labels the cards below
 * without drawing the eye. Indented to line up with the card title text below it, not the card edge.
 */
@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.spaceLg,
                    top = Dimens.spaceXl,
                    bottom = Dimens.spaceMd,
                ),
    )
}
