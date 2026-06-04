package com.llsl.viper4android.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp
import com.llsl.viper4android.ui.theme.Dimens

/**
 * Where a card sits within a "segmented" group — the signature effect list, where a run of rows
 * reads as one rounded container split by thin seams. The ends of the group get large outer
 * corners (20dp); the seams between cards get small corners (4dp) and a 2dp gap; everything sits
 * on one surfaceContainer background. This mirrors the reference app's grouped-card look.
 */
enum class CardGroupPosition {
    Top,
    Middle,
    Bottom,
    Single,
    ;

    /** Rounded shape for this position: outer corners at the group's ends, seam corners between. */
    fun shape(): RoundedCornerShape {
        val outer = Dimens.cardCornerOuter // 20dp
        val seam = Dimens.cardCornerSeam // 4dp
        return when (this) {
            Top -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = seam, bottomEnd = seam)
            Middle -> RoundedCornerShape(seam)
            Bottom -> RoundedCornerShape(topStart = seam, topEnd = seam, bottomStart = outer, bottomEnd = outer)
            Single -> RoundedCornerShape(outer)
        }
    }

    /** Gap below the card to the next one in the group (0 on the last card). */
    fun bottomGap() = if (this == Bottom || this == Single) 0.dp else Dimens.cardGap

    companion object {
        /** Position of the [index]th visible card in a group of [count]. */
        fun of(
            index: Int,
            count: Int,
        ): CardGroupPosition =
            when {
                count <= 1 -> Single
                index == 0 -> Top
                index == count - 1 -> Bottom
                else -> Middle
            }
    }
}

/**
 * Carries the current card's [CardGroupPosition] down to descendant [EffectSection]s so each one
 * can pick its corner shape and seam gap without a parameter being threaded through every call.
 * Whoever builds the list provides this per item.
 */
val LocalCardGroupPosition = compositionLocalOf { CardGroupPosition.Single }
