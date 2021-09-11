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
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * @param tooltipStyle Style for tooltip. Can be created by [rememberTooltipStyle]
 * @param tipPosition Value between 0.0 (inclusive) and 1.0 (inclusive) which specifies tip position
 *                    relative to balloon
 * @param anchorPosition Value between 0.0 (inclusive) and 1.0 (inclusive) which specifies tip
 *                       position relative to [anchor]
 * @param margin Margin between tip and [anchor]
 * @param content Content inside balloon. Typically [Text].
 */
@Composable
fun ConstraintLayoutScope.Tooltip(
    anchor: ConstrainedLayoutReference,
    anchorEdge: AnchorEdge,
    modifier: Modifier = Modifier,
    tooltipStyle: TooltipStyle = rememberTooltipStyle(),
    @FloatRange(from = 0.0, to = 1.0) tipPosition: Float = 0.5f,
    anchorPosition: EdgePoint = remember { EdgePoint() },
    margin: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val refs = remember { TooltipReferences(this@Tooltip) }

    AnchorHelpers(
        anchorEdge = anchorEdge,
        anchor = anchor,
        refs = refs,
        margin = margin,
        anchorPosition = anchorPosition,
        tooltipStyle = tooltipStyle
    )
    TooltipImpl(
        anchorEdge = anchorEdge,
        modifier = modifier.constrainAs(refs.tooltipContainer) {
            stickTo(refs.tangent, 0.dp, tipPosition)
        },
        tooltipStyle = tooltipStyle,
        tipPosition = tipPosition,
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
 * @param tooltipStyle Style for tooltip. Can be created by [rememberTooltipStyle]
 * @param tipPosition Value between 0.0 (inclusive) and 1.0 (inclusive) which specifies tip position
 *                    relative to balloon
 * @param anchorPosition Value between 0.0 (inclusive) and 1.0 (inclusive) which specifies tip
 *                       position relative to [anchor]
 * @param margin Margin between tip and [anchor]
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
    tooltipStyle: TooltipStyle = rememberTooltipStyle(),
    @FloatRange(from = 0.0, to = 1.0) tipPosition: Float = 0.5f,
    anchorPosition: EdgePoint = remember { EdgePoint() },
    margin: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit,
) = with(anchorEdge) {
    // I don't know why but AnimatedVisibility without MutableTransitionState does'nt work
    val visibleState = remember { MutableTransitionState(visible) }
    visibleState.targetState = visible

    val refs = remember { TooltipReferences(this@Tooltip) }

    AnchorHelpers(
        anchorEdge = anchorEdge,
        anchor = anchor,
        refs = refs,
        margin = margin,
        anchorPosition = anchorPosition,
        tooltipStyle = tooltipStyle
    )
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = Modifier.constrainAs(refs.tooltipContainer) {
            stickTo(refs.tangent, 0.dp, tipPosition)
        },
        enter = enterTransition,
        exit = exitTransition
    ) {
        TooltipImpl(
            modifier = modifier,
            anchorEdge = anchorEdge,
            tipPosition = tipPosition,
            tooltipStyle = tooltipStyle,
            content = content
        )
    }
}

@Composable
private fun ConstraintLayoutScope.AnchorHelpers(
    anchorEdge: AnchorEdge,
    anchor: ConstrainedLayoutReference,
    refs: TooltipReferences,
    margin: Dp,
    anchorPosition: EdgePoint,
    tooltipStyle: TooltipStyle
) {
    ContactPoint(anchor, anchorEdge, anchorPosition, refs, margin)
    Tangent(anchorEdge, tooltipStyle, refs)
}

@Composable
private fun ConstraintLayoutScope.ContactPoint(
    anchor: ConstrainedLayoutReference,
    anchorEdge: AnchorEdge,
    anchorPosition: EdgePoint,
    refs: TooltipReferences,
    margin: Dp
) = with(anchorEdge) {
    val positionOffset = anchorPosition.margin
    if (positionOffset == 0.dp) {
        Spacer(
            modifier = Modifier
                .size(selectWidth(1.dp, 0.dp), selectHeight(1.dp, 0.dp))
                .constrainAs(refs.contactPoint) {
                    outside(anchor, margin)
                    align(anchor, anchorPosition.percent)
                }
        )
    } else {
        Spacer(
            modifier = Modifier
                .size(0.dp, 0.dp)
                .constrainAs(refs.contactPointOrigin) {
                    align(anchor, anchorPosition.percent)
                }
        )
        Spacer(
            modifier = Modifier
                .size(0.dp, 0.dp)
                .constrainAs(refs.contactPoint) {
                    outside(anchor, margin)
                    if (positionOffset > 0.dp) {
                        nextTo(refs.contactPointOrigin, positionOffset)
                    } else {
                        beforeTo(refs.contactPointOrigin, -positionOffset)
                    }
                }
        )
    }
}

@Composable
private fun ConstraintLayoutScope.Tangent(
    anchorEdge: AnchorEdge,
    tooltipStyle: TooltipStyle,
    refs: TooltipReferences
) = with(anchorEdge) {
    val tangentWidth = tooltipStyle.cornerRadius * 2 + tooltipStyle.tipWidth
    Spacer(
        modifier = Modifier
            .size(selectWidth(tangentWidth, 0.dp), selectHeight(tangentWidth, 0.dp))
            .constrainAs(refs.tangent) {
                stickTo(refs.contactPoint, 0.dp, 0.5f)
            }
    )
}

@Composable
private fun ConstraintLayoutScope.TooltipImpl(
    anchorEdge: AnchorEdge,
    tipPosition: Float,
    modifier: Modifier,
    tooltipStyle: TooltipStyle,
    content: @Composable (RowScope.() -> Unit)
) = with(anchorEdge) {
    TooltipContainer(
        modifier = modifier,
        cornerRadius = tooltipStyle.cornerRadius,
        tipPosition = tipPosition,
        tip = {
            Box(modifier = Modifier
                .size(
                    width = anchorEdge.selectWidth(
                        tooltipStyle.tipWidth,
                        tooltipStyle.tipHeight
                    ),
                    height = anchorEdge.selectHeight(
                        tooltipStyle.tipWidth,
                        tooltipStyle.tipHeight
                    )
                )
                .background(
                    color = tooltipStyle.color,
                    shape = GenericShape { size, layoutDirection ->
                        drawTip(size, layoutDirection)
                    }
                )
            )
        },
        content = {
            Row(
                modifier = Modifier
                    .minSize(tooltipStyle)
                    .background(
                        color = tooltipStyle.color,
                        shape = RoundedCornerShape(tooltipStyle.cornerRadius)
                    )
                    .padding(tooltipStyle.contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides contentColorFor(tooltipStyle.color)
                ) {
                    content()
                }
            }
        }
    )
}

private class TooltipReferences(scope: ConstraintLayoutScope) {
    val contactPointOrigin = scope.createRef()
    val contactPoint = scope.createRef()
    val tangent = scope.createRef()
    val tooltipContainer = scope.createRef()
}