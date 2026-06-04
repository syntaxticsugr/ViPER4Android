package com.llsl.viper4android.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing and sizing tokens for the UI. The values are chosen to match the layout and proportions
 * of the com.wstxda.viper4android app.
 *
 * The signature layout, for reference while reading the tokens below: cards are inset
 * [listSideMargin] (12dp) from the screen edges, share a [cardGap] (2dp) within a group, pad
 * [cardPadding] (16dp) internally, and round [cardCornerOuter] (20dp) on the group's outer
 * corners with [cardCornerSeam] (4dp) on the seams where cards meet.
 */
object Dimens {
    // General-purpose spacing steps, smallest to largest
    val spaceXxs = 2.dp
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val space = 16.dp
    val spaceLg = 20.dp
    val spaceXl = 24.dp
    val spaceXxl = 32.dp

    // Screen / list gutters — cards are inset 12dp from the screen edge, 2dp apart.
    val screenPadding = 12.dp
    val listSideMargin = 12.dp
    val cardGap = 2.dp
    val rowSpacing = 2.dp

    // Grouped effect rows / section headers
    val rowMinHeight = 72.dp
    val cardPadding = 16.dp
    val rowPaddingH = 16.dp
    val rowPaddingV = 16.dp
    val rowIconSize = 24.dp
    val rowIconGap = 16.dp
    val iconGap = 16.dp
    val titleSummaryGap = 2.dp
    val widgetFrameHeight = 36.dp
    val touchTarget = 48.dp

    // Selectable pills (EQ band tabs, preset chips). A fixed height keeps text pills and the
    // icon-only "+" pill exactly the same size so they sit on one line.
    val chipHeight = 36.dp

    // Card group corners
    val cardCornerOuter = 20.dp
    val cardCornerSeam = 4.dp
    val cardCornerStandalone = 28.dp

    // Hero master-toggle card: a primaryContainer-tinted pill with a 48dp corner radius
    val heroMinHeight = 72.dp
    val heroCorner = 48.dp
    val heroMargin = 24.dp
    val heroPaddingH = 24.dp
    val heroPaddingStart = 28.dp
    val heroPaddingEnd = 20.dp

    // Switch (M3 expressive)
    val switchWidth = 52.dp
    val switchTrackCorner = 24.dp
    val switchThumbOn = 24.dp
    val switchThumbOff = 16.dp

    // Slider (thumbless M3)
    val sliderTrackHeight = 20.dp
    val sliderTrackCorner = 28.dp
    val sliderRowHeight = 36.dp
    val sliderRowGap = 8.dp
    val sliderStopDot = 6.dp

    // Dialogs / sheets / chooser rows
    val dialogPadding = 24.dp
    val dialogCorner = 28.dp
    val dialogWidthFraction = 0.91f
    val singleChoiceCorner = 16.dp
    val singleChoiceInsetH = 24.dp
    val bottomSheetContentCorner = 16.dp
    val bottomSheetContentPadding = 12.dp

    // Chrome
    val toolbarMarginH = 8.dp
    val infoFooterPaddingTop = 24.dp
    val infoIconGap = 12.dp
    val snackbarMargin = 17.dp

    // EQ graph — enough room for the bands to swing without leaving a big empty gap above a flat curve.
    val eqGraphHeight = 224.dp
}
