package com.github.skgmn.composetooltip

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.github.skgmn.composetooltip.AnchorEdge.Top.stickTo

/**
 * Show a tooltip near to [anchor].
 *
 * @param anchor [ConstrainedLayoutReference] to locate this tooltip nearby
 * @param anchorEdge Can be either of [AnchorEdge.Start], [AnchorEdge.Top], [AnchorEdge.End],
 *                   [AnchorEdge.Bottom]
 * @param modifier Modifier for tooltip. Do not use layout-related modifiers except size
 *                 constraints.
 * @param color Color of tooltip background
 * @param cornerRadius Corner radius of balloon
 * @param tipPosition Value between 0.0 (inclusive) and 1.0 (inclusive) which specifies tip position
 *                    relative to balloon
 * @param anchorPosition Value between 0.0 (inclusive) and 1.0 (inclusive) which specifies tip
 *                       position relative to [anchor]
 * @param margin Margin between tip and [anchor]
 * @param tipWidth Width of tip
 * @param tipHeight Height of tip
 * @param contentPadding Padding between balloon and [content].
 * @param content Content inside balloon. Typically [Text].
 */
@Composable
fun ConstraintLayoutScope.Tooltip(
    anchor: ConstrainedLayoutReference,
    anchorEdge: AnchorEdge,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.secondary,
    cornerRadius: Dp = 8.dp,
    @FloatRange(from = 0.0, to = 1.0) tipPosition: Float = 0.5f,
    @FloatRange(from = 0.0, to = 1.0) anchorPosition: Float = 0.5f,
    margin: Dp = 8.dp,
    tipWidth: Dp = 24.dp,
    tipHeight: Dp = 8.dp,
    contentPadding: Dp = 12.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val (contactPoint, tangent, tooltipContainer) = createRefs()

    AnchorHelpers(
        anchorEdge = anchorEdge,
        anchor = anchor,
        contactPoint = contactPoint,
        tangent = tangent,
        margin = margin,
        anchorPosition = anchorPosition,
        cornerRadius = cornerRadius,
        tipWidth = tipWidth
    )
    TooltipImpl(
        anchorEdge = anchorEdge,
        modifier = modifier.constrainAs(tooltipContainer) {
            stickTo(tangent, 0.dp, tipPosition)
        },
        cornerRadius = cornerRadius,
        tipPosition = tipPosition,
        tipWidth = tipWidth,
        tipHeight = tipHeight,
        color = color,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Show a tooltip near to [anchor] with transition.
 * As [AnimatedVisibility] is experimental, this function is also experimental.
 *
 * @param anchor [ConstrainedLayoutReference] to locate this tooltip nearby
 * @param anchorEdge Can be either of [AnchorEdge.Start], [AnchorEdge.Top], [AnchorEdge.End],
 *                   [AnchorEdge.Bottom]
 * @param enterTransition [EnterTransition] to be applied when the [visible] becomes true.
 *                        Types of [EnterTransition] are listed [here](https://developer.android.com/jetpack/compose/animation#entertransition).
 * @param exitTransition [ExitTransition] to be applied when the [visible] becomes false.
 *                       Types of [ExitTransition] are listed [here](https://developer.android.com/jetpack/compose/animation#exittransition).
 * @param modifier Modifier for tooltip. Do not use layout-related modifiers except size
 *                 constraints.
 * @param visible Visibility of tooltip
 * @param color Color of tooltip background
 * @param cornerRadius Corner radius of balloon
 * @param tipPosition Value between 0.0 (inclusive) and 1.0 (inclusive) which specifies tip position
 *                    relative to balloon
 * @param anchorPosition Value between 0.0 (inclusive) and 1.0 (inclusive) which specifies tip
 *                       position relative to [anchor]
 * @param margin Margin between tip and [anchor]
 * @param tipWidth Width of tip
 * @param tipHeight Height of tip
 * @param contentPadding Padding between balloon and [content].
 * @param content Content inside balloon. Typically [Text].
 */
@ExperimentalAnimationApi
@Composable
fun ConstraintLayoutScope.Tooltip(
    anchor: ConstrainedLayoutReference,
    anchorEdge: AnchorEdge,
    enterTransition: EnterTransition,
    exitTransition: ExitTransition,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    color: Color = MaterialTheme.colors.secondary,
    cornerRadius: Dp = 8.dp,
    @FloatRange(from = 0.0, to = 1.0) tipPosition: Float = 0.5f,
    @FloatRange(from = 0.0, to = 1.0) anchorPosition: Float = 0.5f,
    margin: Dp = 8.dp,
    tipWidth: Dp = 24.dp,
    tipHeight: Dp = 8.dp,
    contentPadding: Dp = 12.dp,
    content: @Composable RowScope.() -> Unit,
) = with(anchorEdge) {
    // I don't know why but AnimatedVisibility without MutableTransitionState does'nt work
    val visibleState = remember { MutableTransitionState(visible) }
    visibleState.targetState = visible

    val (contactPoint, tangent, tooltipContainer) = createRefs()

    AnchorHelpers(
        anchorEdge = anchorEdge,
        anchor = anchor,
        contactPoint = contactPoint,
        tangent = tangent,
        margin = margin,
        anchorPosition = anchorPosition,
        cornerRadius = cornerRadius,
        tipWidth = tipWidth
    )
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = Modifier.constrainAs(tooltipContainer) {
            stickTo(tangent, 0.dp, tipPosition)
        },
        enter = enterTransition,
        exit = exitTransition
    ) {
        TooltipImpl(
            modifier = modifier,
            anchorEdge = anchorEdge,
            cornerRadius = cornerRadius,
            tipPosition = tipPosition,
            tipWidth = tipWidth,
            tipHeight = tipHeight,
            color = color,
            contentPadding = contentPadding,
            content = content
        )
    }
}

@Composable
private fun ConstraintLayoutScope.AnchorHelpers(
    anchorEdge: AnchorEdge,
    anchor: ConstrainedLayoutReference,
    contactPoint: ConstrainedLayoutReference,
    tangent: ConstrainedLayoutReference,
    margin: Dp,
    anchorPosition: Float,
    cornerRadius: Dp,
    tipWidth: Dp
) = with(anchorEdge) {
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
}

@Composable
private fun ConstraintLayoutScope.TooltipImpl(
    anchorEdge: AnchorEdge,
    cornerRadius: Dp,
    tipPosition: Float,
    tipWidth: Dp,
    tipHeight: Dp,
    color: Color,
    contentPadding: Dp,
    modifier: Modifier,
    content: @Composable() (RowScope.() -> Unit)
) = with(anchorEdge) {
    TooltipContainer(
        modifier = modifier,
        cornerRadius = cornerRadius,
        tipPosition = tipPosition,
        tip = {
            Box(modifier = Modifier
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
        },
        content = {
            Row(
                modifier = Modifier.Companion
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