package com.github.skgmn.composetooltip

import androidx.annotation.FloatRange
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class EdgePosition(
    @FloatRange(from = 0.0, to = 1.0)
    percent: Float = 0.5f,
    offset: Dp = 0.dp
) {
    @get:FloatRange(from = 0.0, to = 1.0)
    @setparam:FloatRange(from = 0.0, to = 1.0)
    var percent by mutableStateOf(percent)
    var offset by mutableStateOf(offset)
}