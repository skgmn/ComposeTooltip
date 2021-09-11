package com.github.skgmn.composetooltip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope

abstract class AnchorEdge {
    internal abstract fun ConstrainScope.stickTo(
        anchor: ConstrainedLayoutReference,
        margin: Dp,
        bias: Float
    )

    @Composable
    internal abstract fun ConstraintLayoutScope.TooltipContainer(
        modifier: Modifier,
        cornerRadius: Dp,
        tipPosition: Float,
        tip: @Composable () -> Unit,
        content: @Composable () -> Unit
    )

    internal abstract fun ConstrainScope.outside(anchor: ConstrainedLayoutReference, margin: Dp)
    internal abstract fun ConstrainScope.align(anchor: ConstrainedLayoutReference, bias: Float)
    internal abstract fun ConstrainScope.nextTo(anchor: ConstrainedLayoutReference, margin: Dp)
    internal abstract fun ConstrainScope.beforeTo(anchor: ConstrainedLayoutReference, margin: Dp)

    internal abstract fun selectWidth(width: Dp, height: Dp): Dp
    internal abstract fun selectHeight(width: Dp, height: Dp): Dp
    internal abstract fun Modifier.minSize(tooltipStyle: TooltipStyle): Modifier
    internal abstract fun Path.drawTip(size: Size, layoutDirection: LayoutDirection)

    abstract class VerticalAnchorEdge : AnchorEdge() {
        override fun ConstrainScope.align(anchor: ConstrainedLayoutReference, bias: Float) {
            linkTo(anchor.top, anchor.bottom, bias = bias)
        }

        override fun ConstrainScope.nextTo(anchor: ConstrainedLayoutReference, margin: Dp) {
            top.linkTo(anchor.bottom, margin)
        }

        override fun ConstrainScope.beforeTo(anchor: ConstrainedLayoutReference, margin: Dp) {
            bottom.linkTo(anchor.top, margin)
        }

        override fun selectWidth(width: Dp, height: Dp): Dp {
            return min(width, height)
        }

        override fun selectHeight(width: Dp, height: Dp): Dp {
            return max(width, height)
        }

        override fun Modifier.minSize(tooltipStyle: TooltipStyle): Modifier = with(tooltipStyle) {
            return heightIn(min = cornerRadius * 2 + max(tipWidth, tipHeight))
        }
    }

    abstract class HorizontalAnchorEdge : AnchorEdge() {
        override fun ConstrainScope.align(anchor: ConstrainedLayoutReference, bias: Float) {
            linkTo(anchor.start, anchor.end, bias = bias)
        }

        override fun ConstrainScope.nextTo(anchor: ConstrainedLayoutReference, margin: Dp) {
            start.linkTo(anchor.end, margin)
        }

        override fun ConstrainScope.beforeTo(anchor: ConstrainedLayoutReference, margin: Dp) {
            end.linkTo(anchor.start, margin)
        }

        override fun selectWidth(width: Dp, height: Dp): Dp {
            return max(width, height)
        }

        override fun selectHeight(width: Dp, height: Dp): Dp {
            return min(width, height)
        }

        override fun Modifier.minSize(tooltipStyle: TooltipStyle): Modifier = with(tooltipStyle) {
            return widthIn(min = cornerRadius * 2 + max(tipWidth, tipHeight))
        }
    }

    object Start : VerticalAnchorEdge() {
        override fun ConstrainScope.stickTo(
            anchor: ConstrainedLayoutReference,
            margin: Dp,
            bias: Float
        ) {
            end.linkTo(anchor.start, margin)
            align(anchor, bias)
        }

        override fun ConstrainScope.outside(anchor: ConstrainedLayoutReference, margin: Dp) {
            end.linkTo(anchor.start, margin)
        }

        override fun Path.drawTip(size: Size, layoutDirection: LayoutDirection) {
            when (layoutDirection) {
                LayoutDirection.Ltr -> {
                    moveTo(0f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(0f, size.height)
                    lineTo(0f, 0f)
                }
                LayoutDirection.Rtl -> {
                    moveTo(size.width, 0f)
                    lineTo(0f, size.height / 2f)
                    lineTo(size.width, size.height)
                    lineTo(size.width, 0f)
                }
            }
        }

        @Composable
        override fun ConstraintLayoutScope.TooltipContainer(
            modifier: Modifier,
            cornerRadius: Dp,
            tipPosition: Float,
            tip: @Composable () -> Unit,
            content: @Composable () -> Unit
        ) {
            ConstraintLayout(modifier = modifier) {
                val (contentContainer, tipContainer) = createRefs()
                Box(
                    modifier = Modifier.constrainAs(contentContainer) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                ) {
                    content()
                }
                Box(
                    modifier = Modifier
                        .constrainAs(tipContainer) {
                            linkTo(
                                contentContainer.top,
                                contentContainer.bottom,
                                bias = tipPosition
                            )
                            start.linkTo(contentContainer.end)
                        }
                        .padding(vertical = cornerRadius)
                ) {
                    tip()
                }
            }
        }
    }

    object Top : HorizontalAnchorEdge() {
        override fun ConstrainScope.stickTo(
            anchor: ConstrainedLayoutReference,
            margin: Dp,
            bias: Float
        ) {
            bottom.linkTo(anchor.top, margin)
            align(anchor, bias)
        }

        override fun ConstrainScope.outside(anchor: ConstrainedLayoutReference, margin: Dp) {
            bottom.linkTo(anchor.top, margin)
        }

        override fun Path.drawTip(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            lineTo(0f, 0f)
        }

        @Composable
        override fun ConstraintLayoutScope.TooltipContainer(
            modifier: Modifier,
            cornerRadius: Dp,
            tipPosition: Float,
            tip: @Composable () -> Unit,
            content: @Composable () -> Unit
        ) {
            ConstraintLayout(modifier = modifier) {
                val (contentContainer, tipContainer) = createRefs()
                Box(
                    modifier = Modifier.constrainAs(contentContainer) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
                ) {
                    content()
                }
                Box(
                    modifier = Modifier
                        .constrainAs(tipContainer) {
                            linkTo(contentContainer.start, contentContainer.end, bias = tipPosition)
                            top.linkTo(contentContainer.bottom)
                        }
                        .padding(horizontal = cornerRadius)
                ) {
                    tip()
                }
            }
        }
    }

    object End : VerticalAnchorEdge() {
        override fun ConstrainScope.stickTo(
            anchor: ConstrainedLayoutReference,
            margin: Dp,
            bias: Float
        ) {
            start.linkTo(anchor.end, margin)
            align(anchor, bias)
        }

        override fun ConstrainScope.outside(anchor: ConstrainedLayoutReference, margin: Dp) {
            start.linkTo(anchor.end, margin)
        }

        override fun Path.drawTip(size: Size, layoutDirection: LayoutDirection) {
            when (layoutDirection) {
                LayoutDirection.Ltr -> {
                    moveTo(size.width, 0f)
                    lineTo(0f, size.height / 2f)
                    lineTo(size.width, size.height)
                    lineTo(size.width, 0f)
                }
                LayoutDirection.Rtl -> {
                    moveTo(0f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(0f, size.height)
                    lineTo(0f, 0f)
                }
            }
        }

        @Composable
        override fun ConstraintLayoutScope.TooltipContainer(
            modifier: Modifier,
            cornerRadius: Dp,
            tipPosition: Float,
            tip: @Composable () -> Unit,
            content: @Composable () -> Unit
        ) {
            ConstraintLayout(modifier = modifier) {
                val (contentContainer, tipContainer) = createRefs()
                Box(
                    modifier = Modifier.constrainAs(contentContainer) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
                ) {
                    content()
                }
                Box(
                    modifier = Modifier
                        .constrainAs(tipContainer) {
                            linkTo(
                                contentContainer.top,
                                contentContainer.bottom,
                                bias = tipPosition
                            )
                            end.linkTo(contentContainer.start)
                        }
                        .padding(vertical = cornerRadius)
                ) {
                    tip()
                }
            }
        }
    }

    object Bottom : HorizontalAnchorEdge() {
        override fun ConstrainScope.stickTo(
            anchor: ConstrainedLayoutReference,
            margin: Dp,
            bias: Float
        ) {
            top.linkTo(anchor.bottom, margin)
            align(anchor, bias)
        }

        override fun ConstrainScope.outside(anchor: ConstrainedLayoutReference, margin: Dp) {
            top.linkTo(anchor.bottom, margin)
        }

        override fun Path.drawTip(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, size.height)
            lineTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
        }

        @Composable
        override fun ConstraintLayoutScope.TooltipContainer(
            modifier: Modifier,
            cornerRadius: Dp,
            tipPosition: Float,
            tip: @Composable () -> Unit,
            content: @Composable () -> Unit
        ) {
            ConstraintLayout(modifier = modifier) {
                val (contentContainer, tipContainer) = createRefs()
                Box(
                    modifier = Modifier.constrainAs(contentContainer) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
                ) {
                    content()
                }
                Box(
                    modifier = Modifier
                        .constrainAs(tipContainer) {
                            linkTo(contentContainer.start, contentContainer.end, bias = tipPosition)
                            bottom.linkTo(contentContainer.top)
                        }
                        .padding(horizontal = cornerRadius)
                ) {
                    tip()
                }
            }
        }
    }
}