package com.github.skgmn.composetooltip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope

@Composable
fun ConstraintLayoutScope.Tooltip(
    anchor: ConstrainedLayoutReference,
    anchorEdge: AnchorEdge,
    color: Color = MaterialTheme.colors.secondary,
    cornerRadius: Dp = 8.dp,
    tipPosition: Float = 0.5f,
    anchorPosition: Float = 0.5f,
    margin: Dp = 8.dp,
    tipWidth: Dp = 24.dp,
    tipHeight: Dp = 8.dp,
    contentPadding: Dp = 12.dp,
    content: @Composable RowScope.() -> Unit,
) = with(anchorEdge) {
    val (contactPoint, tangent, tooltipContainer) = createRefs()

    Spacer(
        modifier = Modifier
            .size(selectWidth(1.dp, 0.dp), selectHeight(1.dp, 0.dp))
            .constrainAs(contactPoint) {
                stickTo(anchor, margin, anchorPosition)
            }
    )
    val tangentWidth = cornerRadius * 2 + tipWidth
    Spacer(
        modifier = Modifier
            .size(selectWidth(tangentWidth, 0.dp), selectHeight(tangentWidth, 0.dp))
            .constrainAs(tangent) {
                stickTo(contactPoint, 0.dp, 0.5f)
            }
    )
    TooltipContainer(
        ref = tooltipContainer,
        tangent = tangent,
        cornerRadius = cornerRadius,
        tipPosition = tipPosition,
        tip = {
            Box(modifier = Modifier
                .size(
                    width = selectWidth(tipWidth, tipHeight),
                    height = selectHeight(tipWidth, tipHeight)
                )
                .background(
                    color = color,
                    shape = GenericShape { size, layoutDirection ->
                        drawTip(size, layoutDirection)
                    }
                )
            )
        },
        content = {
            Row(
                modifier = Modifier
                    .minSize(cornerRadius, tipWidth, tipHeight)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides contentColorFor(color)
                ) {
                    content()
                }
            }
        }
    )
}