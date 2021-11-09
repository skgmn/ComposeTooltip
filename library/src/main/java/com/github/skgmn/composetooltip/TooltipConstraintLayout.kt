package com.github.skgmn.composetooltip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope

/**
 * Show a tooltip near to [anchor].
 *
 * @param anchor [ConstrainedLayoutReference] to locate this tooltip nearby
 * @param anchorEdge Can be either of [AnchorEdge.Start], [AnchorEdge.Top], [AnchorEdge.End],
 *                   or [AnchorEdge.Bottom]
 * @param modifier Modifier for tooltip. Do not use layout-related modifiers except size
 *                 constraints.
 * @param tooltipStyle Style for tooltip. Can be created by [rememberTooltipStyle]
 * @param tipPosition Tip position relative to balloon
 * @param anchorPosition Position on the [anchor]'s edge where the tip points out.
 * @param margin Margin between tip and [anchor]
 * @param content Content inside balloon. Typically [Text].
 */
@Composable
fun ConstraintLayoutScope.Tooltip(
    anchor: ConstrainedLayoutReference,
    anchorEdge: AnchorEdge,
    modifier: Modifier = Modifier,
    tooltipStyle: TooltipStyle = rememberTooltipStyle(),
    tipPosition: EdgePosition = remember { EdgePosition() },
    anchorPosition: EdgePosition = remember { EdgePosition() },
    margin: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit,
) = with(anchorEdge) {
    val refs = remember { TooltipReferences(this@Tooltip) }

    AnchorHelpers(
        anchorEdge = anchorEdge,
        anchor = anchor,
        refs = refs,
        margin = margin,
        anchorPosition = anchorPosition,
        tipPosition = tipPosition,
        tooltipStyle = tooltipStyle
    )
    TooltipImpl(
        anchorEdge = anchorEdge,
        modifier = modifier.constrainAs(refs.tooltipContainer) {
            outside(refs.tangent, 0.dp)
            align(refs.tangent, tipPosition.percent)
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
 *                   or [AnchorEdge.Bottom]
 * @param enterTransition [EnterTransition] to be applied when the [visible] becomes true.
 *                        Types of [EnterTransition] are listed [here](https://developer.android.com/jetpack/compose/animation#entertransition).
 * @param exitTransition [ExitTransition] to be applied when the [visible] becomes false.
 *                       Types of [ExitTransition] are listed [here](https://developer.android.com/jetpack/compose/animation#exittransition).
 * @param modifier Modifier for tooltip. Do not use layout-related modifiers except size
 *                 constraints.
 * @param visible Visibility of tooltip
 * @param tooltipStyle Style for tooltip. Can be created by [rememberTooltipStyle]
 * @param tipPosition Tip position relative to balloon
 * @param anchorPosition Position on the [anchor]'s edge where the tip points out.
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
    tipPosition: EdgePosition = remember { EdgePosition() },
    anchorPosition: EdgePosition = remember { EdgePosition() },
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
        tipPosition = tipPosition,
        tooltipStyle = tooltipStyle
    )
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = Modifier.constrainAs(refs.tooltipContainer) {
            outside(refs.tangent, 0.dp)
            align(refs.tangent, tipPosition.percent)
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
    anchorPosition: EdgePosition,
    tipPosition: EdgePosition,
    tooltipStyle: TooltipStyle
) {
    ContactPoint(anchor, anchorEdge, anchorPosition, refs, margin)
    Tangent(anchorEdge, tooltipStyle, refs, tipPosition)
}

@Composable
private fun ConstraintLayoutScope.ContactPoint(
    anchor: ConstrainedLayoutReference,
    anchorEdge: AnchorEdge,
    anchorPosition: EdgePosition,
    refs: TooltipReferences,
    margin: Dp
) = with(anchorEdge) {
    val positionOffset = anchorPosition.offset
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
    refs: TooltipReferences,
    tipPosition: EdgePosition
) = with(anchorEdge) {
    val tangentWidth = tooltipStyle.cornerRadius * 2 +
            tipPosition.offset.absoluteValue * 2 +
            max(tooltipStyle.tipWidth, tooltipStyle.tipHeight)
    Spacer(
        modifier = Modifier
            .size(selectWidth(tangentWidth, 0.dp), selectHeight(tangentWidth, 0.dp))
            .constrainAs(refs.tangent) {
                outside(refs.contactPoint, 0.dp)
                align(refs.contactPoint, 0.5f)
            }
    )
}

@Composable
private fun ConstraintLayoutScope.TooltipImpl(
    anchorEdge: AnchorEdge,
    tipPosition: EdgePosition,
    modifier: Modifier,
    tooltipStyle: TooltipStyle,
    content: @Composable RowScope.() -> Unit
) = with(anchorEdge) {
    TooltipContainer(
        modifier = modifier,
        cornerRadius = tooltipStyle.cornerRadius,
        tipPosition = tipPosition,
        tip = { Tip(anchorEdge, tooltipStyle) },
        content = { TooltipContentContainer(anchorEdge, tooltipStyle, content) }
    )
}

@Composable
internal fun Tip(anchorEdge: AnchorEdge, tooltipStyle: TooltipStyle) = with(anchorEdge) {
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
        .border(
            width = tooltipStyle.border.borderWidth,
            color = tooltipStyle.border.borderColor,
            shape = GenericShape { size, layoutDirection ->
                this.drawTipBorder(size, layoutDirection)
            }
        )
        .background(
            color = tooltipStyle.color,
            shape = GenericShape { size, layoutDirection ->
                this.drawTip(size, layoutDirection)
            }
        )
    )
}

@Composable
internal fun TooltipContentContainer(
    anchorEdge: AnchorEdge,
    tooltipStyle: TooltipStyle,
    content: @Composable (RowScope.() -> Unit)
) = with(anchorEdge) {
    Row(
        modifier = Modifier.Companion
            .minSize(tooltipStyle)
            .border(
                width = tooltipStyle.border.borderWidth,
                color = tooltipStyle.border.borderColor,
                shape = RoundedCornerShape(tooltipStyle.cornerRadius),
            )
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

private class TooltipReferences(scope: ConstraintLayoutScope) {
    val contactPointOrigin = scope.createRef()
    val contactPoint = scope.createRef()
    val tangent = scope.createRef()
    val tooltipContainer = scope.createRef()
}

internal val Dp.absoluteValue get() = if (this < 0.dp) -this else this