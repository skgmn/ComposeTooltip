package com.github.skgmn.composetooltip

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class TooltipStyle internal constructor(
    color: Color,
    cornerRadius: Dp,
    tipWidth: Dp,
    tipHeight: Dp,
    contentPadding: Dp
) {
    var color by mutableStateOf(color)
    var cornerRadius by mutableStateOf(cornerRadius)
    var tipWidth by mutableStateOf(tipWidth)
    var tipHeight by mutableStateOf(tipHeight)
    var contentPadding by mutableStateOf(contentPadding)
}

@Composable
fun rememberTooltipStyle(
    color: Color = MaterialTheme.colors.secondary,
    cornerRadius: Dp = 8.dp,
    tipWidth: Dp = 24.dp,
    tipHeight: Dp = 8.dp,
    contentPadding: Dp = 12.dp,
): TooltipStyle {
    return remember { TooltipStyle(color, cornerRadius, tipWidth, tipHeight, contentPadding) }
}