package com.llsl.viper4android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii for cards, dialogs, and rows, sized to match the rounded look of the
 * com.wstxda.viper4android app:
 *  - extraSmall 4dp  -> the seam corners where two cards meet inside a group
 *  - medium 16dp     -> single-choice rows and the bottom-sheet content card
 *  - large 20dp      -> the outer corners of a card group
 *  - extraLarge 28dp -> dialogs and standalone rows
 *
 * The hero master-toggle pill rounds at 48dp, which is larger than anything in the M3 scale,
 * so that radius lives in [Dimens.heroCorner] instead.
 */
val ViperShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )
