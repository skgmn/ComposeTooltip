package com.github.skgmn.composetooltip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
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
    content: @Composable ColumnScope.() -> Unit,
) {
    val (tip, body) = createRefs()
    with(anchorEdge) {
        Box(
            modifier = Modifier
                .constrainAs(tip) {
                    linkToAnchor(anchor, margin, anchorPosition)
                }
                .tipPadding(cornerRadius)
                .size(
                    width = anchorEdge.selectWidth(tipWidth, tipHeight),
                    height = anchorEdge.selectHeight(tipWidth, tipHeight)
                )
                .background(
                    color = color,
                    shape = GenericShape { size, layoutDirection ->
                        drawTip(size, layoutDirection)
                    }
                )
        )
        Column(
            modifier = Modifier
                .constrainAs(body) {
                    linkToAnchor(tip, 0.dp, tipPosition)
                }
                .minSize(cornerRadius, tipWidth, tipHeight)
                .background(
                    color = color,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(contentPadding)
        ) {
            content()
        }
    }
}