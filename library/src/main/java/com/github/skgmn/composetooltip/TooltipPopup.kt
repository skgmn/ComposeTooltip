package com.github.skgmn.composetooltip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

private const val TRANSITION_INITIALIZE = 0
private const val TRANSITION_ENTER = 1
private const val TRANSITION_EXIT = 2
private const val TRANSITION_GONE = 3

@Composable
fun Tooltip(
    anchorEdge: AnchorEdge,
    modifier: Modifier = Modifier,
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
        TooltipImpl(
            modifier = modifier,
            tooltipStyle = tooltipStyle,
            tipPosition = tipPosition,
            anchorEdge = anchorEdge,
            content = content
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun Tooltip(
    anchorEdge: AnchorEdge,
    enterTransition: EnterTransition,
    exitTransition: ExitTransition,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    tooltipStyle: TooltipStyle = rememberTooltipStyle(),
    tipPosition: EdgePosition = remember { EdgePosition() },
    anchorPosition: EdgePosition = remember { EdgePosition() },
    margin: Dp = 8.dp,
    onDismissRequest: (() -> Unit)? = null,
    properties: PopupProperties = remember { PopupProperties() },
    content: @Composable RowScope.() -> Unit,
) = with(anchorEdge) {
    var transitionState by remember { mutableStateOf(TRANSITION_GONE) }
    LaunchedEffect(visible) {
        if (visible) {
            when (transitionState) {
                TRANSITION_EXIT -> transitionState = TRANSITION_ENTER
                TRANSITION_GONE -> {
                    transitionState = TRANSITION_INITIALIZE
                    delay(1)
                    transitionState = TRANSITION_ENTER
                }
            }
        } else {
            when (transitionState) {
                TRANSITION_INITIALIZE -> transitionState = TRANSITION_GONE
                TRANSITION_ENTER -> transitionState = TRANSITION_EXIT
            }
        }
    }
    if (transitionState != TRANSITION_GONE) {
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
            if (transitionState == TRANSITION_INITIALIZE) {
                TooltipImpl(
                    tooltipStyle = tooltipStyle,
                    tipPosition = tipPosition,
                    anchorEdge = anchorEdge,
                    modifier = modifier.alpha(0f),
                    content = content,
                )
            }
            AnimatedVisibility(
                visible = transitionState == TRANSITION_ENTER,
                enter = enterTransition,
                exit = exitTransition
            ) {
                remember {
                    object : RememberObserver {
                        override fun onAbandoned() {
                            transitionState = TRANSITION_GONE
                        }

                        override fun onForgotten() {
                            transitionState = TRANSITION_GONE
                        }

                        override fun onRemembered() {
                        }
                    }
                }
                TooltipImpl(
                    modifier = modifier,
                    tooltipStyle = tooltipStyle,
                    tipPosition = tipPosition,
                    anchorEdge = anchorEdge,
                    content = content
                )
            }
        }
    }
}

@Composable
private fun AnchorEdge.TooltipImpl(
    tooltipStyle: TooltipStyle,
    tipPosition: EdgePosition,
    anchorEdge: AnchorEdge,
    modifier: Modifier = Modifier,
    content: @Composable() (RowScope.() -> Unit)
) {
    TooltipContainer(
        modifier = modifier,
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