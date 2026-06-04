package com.llsl.viper4android.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.llsl.viper4android.R
import com.llsl.viper4android.audio.EffectDispatcher
import com.llsl.viper4android.data.model.EqPreset
import com.llsl.viper4android.ui.theme.Dimens
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private const val DB_MIN = -12f
private const val DB_MAX = 12f
// Horizontal grid rules every 3 dB across the full ±12 dB range. The outermost lines sit at the
// top and bottom edges, marking the largest boost and cut a band can reach.
private val DB_GRID_LINES = listOf(-12f, -9f, -6f, -3f, 0f, 3f, 6f, 9f, 12f)

/**
 * The EQ preview graph: a smooth spline (drawn in the primary colour) through the band points,
 * over a faint grid, with a soft gradient fill beneath the curve that fades to transparent and
 * frequency labels along the bottom. Tapping invokes [onClick], which normally opens [EqEditDialog].
 */
@Composable
fun EqCurveGraph(
    bands: List<Float>,
    onClick: () -> Unit,
    bandCount: Int = 10,
    modifier: Modifier = Modifier,
) {
    val freqLabels = EffectDispatcher.eqGraphLabelsForCount(bandCount)
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    // A faint wash of onSurface for the grid, so it reads as a backdrop rather than competing
    // with the curve.
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val density = LocalDensity.current

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(Dimens.eqGraphHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onClick() },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paddingLeft = 12.dp.toPx()
            val paddingRight = 12.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingBottom = 28.dp.toPx()

            val graphWidth = size.width - paddingLeft - paddingRight
            val graphHeight = size.height - paddingTop - paddingBottom

            val gridPaint =
                Paint().apply {
                    color = gridColor.toArgb()
                    strokeWidth = with(density) { 1.5.dp.toPx() }
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                }

            val freqTextPaint =
                Paint().apply {
                    color = onSurface.toArgb()
                    textSize = with(density) { (if (bandCount > 15) 7 else 9).dp.toPx() }
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = Typeface.DEFAULT
                }

            val valuePaint =
                Paint().apply {
                    color = onSurface.toArgb()
                    textSize = with(density) { 8.dp.toPx() }
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = Typeface.DEFAULT_BOLD
                }

            // Draw the faint grid rules at ±3/6/9 dB. There are no axis labels by design.
            for (db in DB_GRID_LINES) {
                val y = paddingTop + graphHeight * (1f - (db - DB_MIN) / (DB_MAX - DB_MIN))
                drawContext.canvas.nativeCanvas.drawLine(
                    paddingLeft,
                    y,
                    size.width - paddingRight,
                    y,
                    gridPaint,
                )
            }

            if (bands.size < bandCount) return@Canvas

            val points =
                bands.take(bandCount).mapIndexed { i, db ->
                    val x = paddingLeft + graphWidth * i / (bandCount - 1).toFloat()
                    val y =
                        paddingTop + graphHeight * (
                            1f - (
                                db.coerceIn(
                                    DB_MIN,
                                    DB_MAX,
                                ) - DB_MIN
                            ) / (DB_MAX - DB_MIN)
                        )
                    Offset(x, y)
                }

            val curvePath = buildSplinePath(points)

            val fillPath =
                Path().apply {
                    addPath(curvePath)
                    lineTo(points.last().x, paddingTop + graphHeight)
                    lineTo(points.first().x, paddingTop + graphHeight)
                    close()
                }

            drawPath(
                path = fillPath,
                brush =
                    Brush.verticalGradient(
                        colors = listOf(primary.copy(alpha = 0.45f), Color.Transparent),
                        startY = paddingTop,
                        endY = paddingTop + graphHeight,
                    ),
            )

            drawPath(
                path = curvePath,
                color = primary,
                style = Stroke(width = 2.5.dp.toPx()),
            )

            val labelStep =
                when (bandCount) {
                    31 -> 5
                    25 -> 4
                    15 -> 2
                    else -> 1
                }
            val showValues = bandCount <= 15

            points.forEachIndexed { i, pt ->
                drawCircle(
                    color = primary,
                    radius = (if (bandCount > 15) 3 else 5).dp.toPx(),
                    center = pt,
                )

                if (i % labelStep == 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        freqLabels.getOrElse(i) { "" },
                        pt.x,
                        paddingTop + graphHeight + with(density) { 14.dp.toPx() },
                        freqTextPaint,
                    )
                }

                if (showValues) {
                    val valText = "%.1f".format(bands[i])
                    drawContext.canvas.nativeCanvas.drawText(
                        valText,
                        pt.x,
                        pt.y - with(density) { 6.dp.toPx() },
                        valuePaint,
                    )
                }
            }
        }
    }
}

private fun buildSplinePath(points: List<Offset>): Path {
    val path = Path()
    val n = points.size
    if (n == 0) return path
    path.moveTo(points[0].x, points[0].y)
    if (n == 1) return path
    if (n == 2) {
        path.lineTo(points[1].x, points[1].y)
        return path
    }

    val tension = 0.3f
    val damping = 0.15f

    for (i in 0 until n - 1) {
        val prev = points[max(0, i - 1)]
        val curr = points[i]
        val next = points[i + 1]
        val afterNext = points[min(n - 1, i + 2)]

        var t1 = tension
        val isLocalMax = curr.y <= prev.y && curr.y <= next.y
        val isLocalMin = curr.y >= prev.y && curr.y >= next.y
        if (isLocalMax || isLocalMin) t1 = damping

        var t2 = tension
        val isNextLocalMax = next.y <= curr.y && next.y <= afterNext.y
        val isNextLocalMin = next.y >= curr.y && next.y >= afterNext.y
        if (isNextLocalMax || isNextLocalMin) t2 = damping

        val cp1x = curr.x + (next.x - prev.x) * t1
        val cp1y = curr.y + (next.y - prev.y) * t1
        val cp2x = next.x - (afterNext.x - curr.x) * t2
        val cp2y = next.y - (afterNext.y - curr.y) * t2

        path.cubicTo(cp1x, cp1y, cp2x, cp2y, next.x, next.y)
    }
    return path
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqEditDialog(
    bands: List<Float>,
    onBandsChange: (String) -> Unit,
    presetId: Long?,
    presets: List<EqPreset>,
    onPresetSelect: (Long) -> Unit,
    onPresetAdd: (String) -> Unit,
    onPresetDelete: (Long) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    bandCount: Int = 10,
) {
    val localBands =
        remember(bandCount) {
            mutableStateListOf<Float>().apply { addAll(bands.take(bandCount)) }
        }

    LaunchedEffect(bands) {
        val incoming = bands.take(bandCount)
        if (incoming != localBands.toList()) {
            localBands.clear()
            localBands.addAll(incoming)
        }
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Dimens.dialogCorner),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(vertical = Dimens.dialogPadding)
                        .verticalScroll(rememberScrollState()),
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = Dimens.spaceSm),
                )
                Text(
                    text = stringResource(R.string.section_equalizer),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = Dimens.dialogPadding, end = Dimens.dialogPadding, bottom = Dimens.space),
                )

                Column(modifier = Modifier.padding(horizontal = Dimens.dialogPadding)) {
                    EqCurveGraph(
                        bands = localBands.toList(),
                        onClick = {},
                        bandCount = bandCount,
                    )

                    Spacer(modifier = Modifier.height(Dimens.space))

                    // Horizontally scrolling row of preset chips (Flat, Deep, Bass booster, and so on).
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    ) {
                        presets.forEach { preset ->
                            PresetChip(
                                label = resolvePresetName(preset),
                                selected = preset.id == presetId,
                                onClick = { onPresetSelect(preset.id) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.spaceSm))

                    // Save-as, Reset, and (only for a saved preset) Delete actions.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    ) {
                        TextButton(onClick = { showSaveDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(Dimens.spaceXs))
                            Text(stringResource(R.string.action_save))
                        }
                        TextButton(onClick = {
                            for (i in localBands.indices) {
                                localBands[i] = 0f
                            }
                            onReset()
                        }) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(Dimens.spaceXs))
                            Text(stringResource(R.string.action_reset))
                        }
                        if (presetId != null) {
                            TextButton(onClick = { onPresetDelete(presetId) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(Dimens.spaceXs))
                                Text(stringResource(R.string.action_delete))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.spaceSm))

                    val bandLabels = EffectDispatcher.eqBandLabelsForCount(bandCount)

                    bandLabels.forEachIndexed { index, label ->
                        if (index < localBands.size) {
                            val applyBandChange = { newVal: Float ->
                                localBands[index] = newVal.coerceIn(DB_MIN, DB_MAX)
                                val str =
                                    localBands.joinToString(";") {
                                        String.format(Locale.US, "%.1f", it)
                                    } + ";"
                                onBandsChange(str)
                            }

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Dimens.spaceXxs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(48.dp),
                                )
                                Slider(
                                    value = localBands[index],
                                    onValueChange = { applyBandChange(it) },
                                    valueRange = DB_MIN..DB_MAX,
                                    modifier = Modifier.weight(1f),
                                    colors =
                                        SliderDefaults.colors(
                                            activeTickColor = Color.Transparent,
                                            inactiveTickColor = Color.Transparent,
                                        ),
                                )
                                Text(
                                    text = "${"%.1f".format(localBands[index])}dB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(52.dp),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceSm))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.space),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.preset_save_title)) },
            text = {
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text(stringResource(R.string.preset_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            onPresetAdd(presetNameInput.trim())
                            presetNameInput = ""
                            showSaveDialog = false
                        }
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}
