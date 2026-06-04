package com.llsl.viper4android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.llsl.viper4android.ui.theme.Dimens
import kotlin.math.roundToInt

/**
 * A labelled slider laid out as a [label] on its own line, then a row holding the track and the
 * current [value] (or [valueLabel]) right-aligned. The track is thumbless — a primary fill over a
 * secondaryContainer pill with a small stop-dot near the end — to match the reference app's look.
 * Tick marks are suppressed so the track stays clean.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    valueLabel: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = Dimens.spaceSm)) {
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                enabled = enabled,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(Dimens.sliderRowHeight),
                thumb = {},
                track = { state -> ViperSliderTrack(state, enabled) },
            )
            Text(
                text = valueLabel ?: value.roundToInt().toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier =
                    Modifier
                        .padding(start = Dimens.spaceSm)
                        .widthIn(min = 44.dp),
            )
        }
    }
}

/**
 * The thumbless track itself: a fully-rounded pill (height [Dimens.sliderTrackHeight]) with a
 * primary fill drawn over a secondaryContainer base, plus a small primary stop-dot sitting a few
 * dp in from the trailing end. Colours dim when disabled. This mirrors the reference app's seekbar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViperSliderTrack(
    state: SliderState,
    enabled: Boolean,
) {
    val range = state.valueRange.endInclusive - state.valueRange.start
    val fraction =
        if (range <= 0f) 0f else ((state.value - state.valueRange.start) / range).coerceIn(0f, 1f)

    val baseColor =
        if (enabled) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }
    val fillColor =
        if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(Dimens.sliderTrackHeight),
    ) {
        val h = size.height
        val w = size.width
        val r = h / 2f
        // The full-width base pill.
        drawRoundRect(color = baseColor, size = Size(w, h), cornerRadius = CornerRadius(r, r))
        // The active fill. Clamp its width to at least the height so even a tiny value still
        // shows a rounded blob rather than a sliver.
        if (fraction > 0f) {
            val fillW = (w * fraction).coerceAtLeast(h)
            drawRoundRect(color = fillColor, size = Size(fillW, h), cornerRadius = CornerRadius(r, r))
        }
        // The stop-dot pinned near the trailing end.
        val dotRadius = Dimens.sliderStopDot.toPx() / 2f
        val cx = w - 7.dp.toPx() - dotRadius
        drawCircle(color = fillColor, radius = dotRadius, center = Offset(cx, h / 2f))
    }
}
