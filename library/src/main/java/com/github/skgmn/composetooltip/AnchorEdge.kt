package com.github.skgmn.composetooltip

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import kotlin.math.roundToInt

abstract class AnchorEdge {
    @Composable
    internal abstract fun TooltipContainer(
        modifier: Modifier,
        cornerRadius: Dp,
        tipPosition: EdgePosition,
        tip: @Composable () -> Unit,
        content: @Composable () -> Unit
    )

    internal open fun calculatePopupPosition(
        density: Density,
        tooltipStyle: TooltipStyle,
        tipPosition: EdgePosition,
        anchorPosition: EdgePosition,
        margin: Dp,
        anchorBounds: IntRect,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset(0, 0)

    internal abstract fun ConstrainScope.outside(anchor: ConstrainedLayoutReference, margin: Dp)
    internal abstract fun ConstrainScope.align(anchor: ConstrainedLayoutReference, bias: Float)
    internal abstract fun ConstrainScope.nextTo(anchor: ConstrainedLayoutReference, margin: Dp)
    internal abstract fun ConstrainScope.beforeTo(anchor: ConstrainedLayoutReference, margin: Dp)

    internal abstract fun selectWidth(width: Dp, height: Dp): Dp
    internal abstract fun selectHeight(width: Dp, height: Dp): Dp
    internal abstract fun Modifier.minSize(tooltipStyle: TooltipStyle): Modifier
    internal abstract fun Path.drawTip(size: Size, layoutDirection: LayoutDirection)
    internal abstract fun Path.drawTipBorder(size: Size, layoutDirection: LayoutDirection)

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

        protected fun calculatePopupPositionY(
            density: Density,
            anchorBounds: IntRect,
            anchorPosition: EdgePosition,
            tooltipStyle: TooltipStyle,
            tipPosition: EdgePosition,
            popupContentSize: IntSize
        ): Float = with(density) {
            val contactPointY = anchorBounds.top +
                    anchorBounds.height * anchorPosition.percent +
                    anchorPosition.offset.toPx()
            val tangentHeight = (tooltipStyle.cornerRadius * 2 +
                    tipPosition.offset.absoluteValue * 2 +
                    max(tooltipStyle.tipWidth, tooltipStyle.tipHeight)).toPx()
            val tangentY = contactPointY - tangentHeight / 2
            val tipMarginY = (popupContentSize.height - tangentHeight) * tipPosition.percent
            val y = tangentY - tipMarginY
            return y
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

        protected fun calculatePopupPositionX(
            density: Density,
            layoutDirection: LayoutDirection,
            anchorBounds: IntRect,
            anchorPosition: EdgePosition,
            tooltipStyle: TooltipStyle,
            tipPosition: EdgePosition,
            popupContentSize: IntSize
        ): Float = with(density) {
            val contactPointX = if (layoutDirection == LayoutDirection.Ltr) {
                anchorBounds.left +
                        anchorBounds.width * anchorPosition.percent +
                        anchorPosition.offset.toPx()
            } else {
                anchorBounds.right -
                        anchorBounds.width * anchorPosition.percent -
                        anchorPosition.offset.toPx()
            }
            val tangentWidth = (tooltipStyle.cornerRadius * 2 +
                    tipPosition.offset.absoluteValue * 2 +
                    max(tooltipStyle.tipWidth, tooltipStyle.tipHeight)).toPx()
            val tangentLeft = contactPointX - tangentWidth / 2
            val tipMarginLeft = (popupContentSize.width - tangentWidth) *
                    if (layoutDirection == LayoutDirection.Ltr) {
                        tipPosition.percent
                    } else {
                        1f - tipPosition.percent
                    }
            val x = tangentLeft - tipMarginLeft
            return x
        }
    }

    object Start : VerticalAnchorEdge() {
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

        override fun Path.drawTipBorder(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, 0f)
            lineTo(size.width / 2f, size.height)
            lineTo(size.width , 0f)
//            close()
        }

        @Composable
        override fun TooltipContainer(
            modifier: Modifier,
            cornerRadius: Dp,
            tipPosition: EdgePosition,
            tip: @Composable () -> Unit,
            content: @Composable () -> Unit
        ) {
            val tipPositionOffset = tipPosition.offset
            ConstraintLayout(modifier = modifier) {
                val (contentContainer, tipContainer) = createRefs()
                Box(
                    modifier = Modifier
                        .constrainAs(contentContainer) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                        .padding(
                            top = if (tipPositionOffset < 0.dp) tipPositionOffset * -2 else 0.dp,
                            bottom = if (tipPositionOffset > 0.dp) tipPositionOffset * 2 else 0.dp
                        )
                ) {
                    content()
                }
                val tipPadding = cornerRadius + tipPositionOffset.absoluteValue
                Box(
                    modifier = Modifier
                        .constrainAs(tipContainer) {
                            linkTo(
                                contentContainer.top,
                                contentContainer.bottom,
                                bias = tipPosition.percent
                            )
                            start.linkTo(contentContainer.end)
                        }
                        .padding(top = tipPadding, bottom = tipPadding)
                ) {
                    tip()
                }
            }
        }

        override fun calculatePopupPosition(
            density: Density,
            tooltipStyle: TooltipStyle,
            tipPosition: EdgePosition,
            anchorPosition: EdgePosition,
            margin: Dp,
            anchorBounds: IntRect,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
        ): IntOffset = with(density) {
            val y = calculatePopupPositionY(
                density,
                anchorBounds,
                anchorPosition,
                tooltipStyle,
                tipPosition,
                popupContentSize
            )
            val x = if (layoutDirection == LayoutDirection.Ltr) {
                anchorBounds.left - margin.toPx() - popupContentSize.width
            } else {
                anchorBounds.right + margin.toPx()
            }
            return IntOffset(x.roundToInt(), y.roundToInt())
        }
    }

    object Top : HorizontalAnchorEdge() {
        override fun ConstrainScope.outside(anchor: ConstrainedLayoutReference, margin: Dp) {
            bottom.linkTo(anchor.top, margin)
        }

        override fun Path.drawTip(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            lineTo(0f, 0f)
        }

        override fun Path.drawTipBorder(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, 0f)
            lineTo(size.width / 2f, size.height)
            lineTo(size.width , 0f)
            moveTo(0f, 0f)
            close()
        }

        @Composable
        override fun TooltipContainer(
            modifier: Modifier,
            cornerRadius: Dp,
            tipPosition: EdgePosition,
            tip: @Composable () -> Unit,
            content: @Composable () -> Unit
        ) {
            val tipPositionOffset = tipPosition.offset
            ConstraintLayout(modifier = modifier) {
                val (contentContainer, tipContainer) = createRefs()
                Box(
                    modifier = Modifier
                        .constrainAs(contentContainer) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                        }
                        .padding(
                            start = if (tipPositionOffset < 0.dp) tipPositionOffset * -2 else 0.dp,
                            end = if (tipPositionOffset > 0.dp) tipPositionOffset * 2 else 0.dp
                        )
                ) {
                    content()
                }
                val tipPadding = cornerRadius + tipPositionOffset.absoluteValue
                Box(
                    modifier = Modifier
                        .constrainAs(tipContainer) {
                            linkTo(
                                contentContainer.start,
                                contentContainer.end,
                                bias = tipPosition.percent
                            )
                            top.linkTo(contentContainer.bottom, margin = (-2.1).dp)
                        }
                        .padding(start = tipPadding, end = tipPadding)
                ) {
                    tip()
                }
            }
        }

        override fun calculatePopupPosition(
            density: Density,
            tooltipStyle: TooltipStyle,
            tipPosition: EdgePosition,
            anchorPosition: EdgePosition,
            margin: Dp,
            anchorBounds: IntRect,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
        ): IntOffset = with(density) {
            val x = calculatePopupPositionX(
                density,
                layoutDirection,
                anchorBounds,
                anchorPosition,
                tooltipStyle,
                tipPosition,
                popupContentSize
            )
            val y = anchorBounds.top - margin.toPx() - popupContentSize.height
            return IntOffset(x.roundToInt(), y.roundToInt())
        }
    }

    object End : VerticalAnchorEdge() {
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

        override fun Path.drawTipBorder(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, 0f)
            lineTo(size.width / 2f, size.height)
            lineTo(size.width , 0f)
//            close()
        }

        @Composable
        override fun TooltipContainer(
            modifier: Modifier,
            cornerRadius: Dp,
            tipPosition: EdgePosition,
            tip: @Composable () -> Unit,
            content: @Composable () -> Unit
        ) {
            val tipPositionOffset = tipPosition.offset
            ConstraintLayout(modifier = modifier) {
                val (contentContainer, tipContainer) = createRefs()
                Box(
                    modifier = Modifier
                        .constrainAs(contentContainer) {
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .padding(
                            top = if (tipPositionOffset < 0.dp) tipPositionOffset * -2 else 0.dp,
                            bottom = if (tipPositionOffset > 0.dp) tipPositionOffset * 2 else 0.dp
                        )
                ) {
                    content()
                }
                val tipPadding = cornerRadius + tipPositionOffset.absoluteValue
                Box(
                    modifier = Modifier
                        .constrainAs(tipContainer) {
                            linkTo(
                                contentContainer.top,
                                contentContainer.bottom,
                                bias = tipPosition.percent
                            )
                            end.linkTo(contentContainer.start)
                        }
                        .padding(top = tipPadding, bottom = tipPadding)
                ) {
                    tip()
                }
            }
        }

        override fun calculatePopupPosition(
            density: Density,
            tooltipStyle: TooltipStyle,
            tipPosition: EdgePosition,
            anchorPosition: EdgePosition,
            margin: Dp,
            anchorBounds: IntRect,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
        ): IntOffset = with(density) {
            val y = calculatePopupPositionY(
                density,
                anchorBounds,
                anchorPosition,
                tooltipStyle,
                tipPosition,
                popupContentSize
            )
            val x = if (layoutDirection == LayoutDirection.Ltr) {
                anchorBounds.right + margin.toPx()
            } else {
                anchorBounds.left - margin.toPx() - popupContentSize.width
            }
            return IntOffset(x.roundToInt(), y.roundToInt())
        }
    }

    object Bottom : HorizontalAnchorEdge() {
        override fun ConstrainScope.outside(anchor: ConstrainedLayoutReference, margin: Dp) {
            top.linkTo(anchor.bottom, margin)
        }

        override fun Path.drawTip(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, size.height)
            lineTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
        }

        override fun Path.drawTipBorder(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, size.height)
            lineTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            moveTo(0f, size.height)


            close()
        }

        @Composable
        override fun TooltipContainer(
            modifier: Modifier,
            cornerRadius: Dp,
            tipPosition: EdgePosition,
            tip: @Composable () -> Unit,
            content: @Composable () -> Unit
        ) {
            val tipPositionOffset = tipPosition.offset
            ConstraintLayout(modifier = modifier) {
                val (contentContainer, tipContainer) = createRefs()
                Box(
                    modifier = Modifier
                        .constrainAs(contentContainer) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .padding(
                            start = if (tipPositionOffset < 0.dp) tipPositionOffset * -2 else 0.dp,
                            end = if (tipPositionOffset > 0.dp) tipPositionOffset * 2 else 0.dp
                        )
                ) {
                    content()
                }
                val tipPadding = cornerRadius + tipPositionOffset.absoluteValue
                Box(
                    modifier = Modifier
                        .constrainAs(tipContainer) {

                            linkTo(
                                contentContainer.start,
                                contentContainer.end,
                                bias = tipPosition.percent
                            )

                            bottom.linkTo(contentContainer.top, margin = (-2.1).dp)
                        }
                        .padding(start = tipPadding, end = tipPadding)
                ) {
                    tip()
                }
            }
        }

        override fun calculatePopupPosition(
            density: Density,
            tooltipStyle: TooltipStyle,
            tipPosition: EdgePosition,
            anchorPosition: EdgePosition,
            margin: Dp,
            anchorBounds: IntRect,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
        ): IntOffset = with(density) {
            val x = calculatePopupPositionX(
                density,
                layoutDirection,
                anchorBounds,
                anchorPosition,
                tooltipStyle,
                tipPosition,
                popupContentSize
            )
            val y = anchorBounds.bottom + margin.toPx()
            return IntOffset(x.roundToInt(), y.roundToInt())
        }
    }
}