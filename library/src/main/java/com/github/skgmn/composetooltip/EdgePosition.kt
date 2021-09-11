package com.github.skgmn.composetooltip

import androidx.annotation.FloatRange
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Specifies a position on an edge.
 */
class EdgePosition(
    @FloatRange(from = 0.0, to = 1.0)
    percent: Float = 0.5f,
    offset: Dp = 0.dp
) {
    /**
     * When it comes to either [AnchorEdge.Top] or [AnchorEdge.Bottom],
     * percent 0.0 means the horizontal start position of the anchor,
     * and percent 1.0 means the horizontal end position of the anchor.
     *
     * If it comes to either [AnchorEdge.Start] or [AnchorEdge.End],
     * percent 0.0 means the top of the anchor, and percent 1.0 means the bottom of the anchor.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    @setparam:FloatRange(from = 0.0, to = 1.0)
    var percent by mutableStateOf(percent)

    /**
     * Amount of dps from the percentage position on the edge.
     *
     * For example, if [percent] is 0.5 and [offset] is 10.dp, tip will point out the location
     * of 10.dp from the center of the edge.
     *
     * This allows negative value.
     */
    var offset by mutableStateOf(offset)
}