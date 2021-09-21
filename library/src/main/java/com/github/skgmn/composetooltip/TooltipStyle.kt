package com.github.skgmn.composetooltip

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Style of tooltip
 */
class TooltipStyle internal constructor(
    color: Color,
    cornerRadius: Dp,
    tipWidth: Dp,
    tipHeight: Dp,
    contentPadding: PaddingValues
) {
    /**
     * Background color of tooltip.
     */
    var color by mutableStateOf(color)

    /**
     * Corner radius of balloon.
     */
    var cornerRadius by mutableStateOf(cornerRadius)

    /**
     * Width of tip.
     */
    var tipWidth by mutableStateOf(tipWidth)

    /**
     * Height of tip.
     */
    var tipHeight by mutableStateOf(tipHeight)

    /**
     * Padding between balloon and content.
     */
    var contentPadding by mutableStateOf(contentPadding)
}

/**
 * Create a [TooltipStyle] and remember it.
 *
 * @param color Background color of tooltip. By default, it uses the seconday color of
 *   [MaterialTheme].
 * @param cornerRadius Corner radius of balloon.
 * @param tipWidth Width of tip.
 * @param tipHeight Height of tip.
 * @param contentPadding Padding between balloon and content.
 */
@Composable
fun rememberTooltipStyle(
    color: Color = MaterialTheme.colors.secondary,
    cornerRadius: Dp = 8.dp,
    tipWidth: Dp = 24.dp,
    tipHeight: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
): TooltipStyle {
    return remember { TooltipStyle(color, cornerRadius, tipWidth, tipHeight, contentPadding) }
}