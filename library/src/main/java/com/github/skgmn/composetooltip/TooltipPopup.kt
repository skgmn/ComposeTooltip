package com.github.skgmn.composetooltip

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun Tooltip(
    anchorEdge: AnchorEdge,
    tooltipStyle: TooltipStyle = rememberTooltipStyle(),
    tipPosition: EdgePosition = remember { EdgePosition() },
    anchorPosition: EdgePosition = remember { EdgePosition() },
    margin: Dp = 8.dp,
    onDismissRequest: (() -> Unit)? = null,
    properties: PopupProperties = remember { PopupProperties() },
    content: @Composable RowScope.() -> Unit,
) = with(anchorEdge) {
    Popup(
        popupPositionProvider = TooltipPopupPositionProvider(
            LocalDensity.current,
            anchorEdge,
            tooltipStyle,
            tipPosition,
            anchorPosition,
            margin
        ),
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        TooltipContainer(
            modifier = Modifier,
            cornerRadius = tooltipStyle.cornerRadius,
            tipPosition = tipPosition,
            tip = { Tip(anchorEdge, tooltipStyle) },
            content = {
                TooltipContentContainer(
                    anchorEdge = anchorEdge,
                    tooltipStyle = tooltipStyle,
                    content = content
                )
            }
        )
    }
}

private class TooltipPopupPositionProvider(
    private val density: Density,
    private val anchorEdge: AnchorEdge,
    private val tooltipStyle: TooltipStyle,
    private val tipPosition: EdgePosition,
    private val anchorPosition: EdgePosition,
    private val margin: Dp
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = anchorEdge.calculatePopupPosition(
        density,
        tooltipStyle,
        tipPosition,
        anchorPosition,
        margin,
        anchorBounds,
        layoutDirection,
        popupContentSize
    )
}