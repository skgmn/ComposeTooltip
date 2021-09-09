package com.github.skgmn.composetooltip

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference

abstract class AnchorEdge {
    internal abstract fun ConstrainScope.linkToAnchor(
        anchor: ConstrainedLayoutReference,
        margin: Dp,
        bias: Float
    )

    internal abstract fun selectWidth(width: Dp, height: Dp): Dp
    internal abstract fun selectHeight(width: Dp, height: Dp): Dp
    internal abstract fun Modifier.minSize(cornerRadius: Dp, tipWidth: Dp, tipHeight: Dp): Modifier
    internal abstract fun Modifier.tipPadding(cornerRadius: Dp): Modifier

    internal abstract fun Path.drawTip(size: Size, layoutDirection: LayoutDirection)

    abstract class VerticalAnchorEdge : AnchorEdge() {
        override fun selectWidth(width: Dp, height: Dp): Dp {
            return min(width, height)
        }

        override fun selectHeight(width: Dp, height: Dp): Dp {
            return max(width, height)
        }

        override fun Modifier.tipPadding(cornerRadius: Dp): Modifier {
            return padding(vertical = cornerRadius * 2)
        }

        override fun Modifier.minSize(cornerRadius: Dp, tipWidth: Dp, tipHeight: Dp): Modifier {
            return heightIn(min = cornerRadius * 2 + max(tipWidth, tipHeight))
        }

        protected fun align(
            scope: ConstrainScope,
            anchor: ConstrainedLayoutReference,
            bias: Float
        ) {
            scope.linkTo(
                top = anchor.top,
                bottom = anchor.bottom,
                bias = bias
            )
        }
    }

    abstract class HorizontalAnchorEdge : AnchorEdge() {
        override fun selectWidth(width: Dp, height: Dp): Dp {
            return max(width, height)
        }

        override fun selectHeight(width: Dp, height: Dp): Dp {
            return min(width, height)
        }

        override fun Modifier.minSize(cornerRadius: Dp, tipWidth: Dp, tipHeight: Dp): Modifier {
            return widthIn(min = cornerRadius * 2 + max(tipWidth, tipHeight))
        }

        override fun Modifier.tipPadding(cornerRadius: Dp): Modifier {
            return padding(horizontal = cornerRadius * 2)
        }

        protected fun align(
            scope: ConstrainScope,
            anchor: ConstrainedLayoutReference,
            bias: Float
        ) {
            scope.linkTo(
                start = anchor.start,
                end = anchor.end,
                bias = bias
            )
        }
    }

    object Start : VerticalAnchorEdge() {
        override fun ConstrainScope.linkToAnchor(
            anchor: ConstrainedLayoutReference,
            margin: Dp,
            bias: Float
        ) {
            end.linkTo(anchor.start, margin)
            align(this, anchor, bias)
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
    }

    object Top : HorizontalAnchorEdge() {
        override fun ConstrainScope.linkToAnchor(
            anchor: ConstrainedLayoutReference,
            margin: Dp,
            bias: Float
        ) {
            bottom.linkTo(anchor.top, margin)
            align(this, anchor, bias)
        }

        override fun Path.drawTip(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            lineTo(0f, 0f)
        }
    }

    object End : VerticalAnchorEdge() {
        override fun ConstrainScope.linkToAnchor(
            anchor: ConstrainedLayoutReference,
            margin: Dp,
            bias: Float
        ) {
            start.linkTo(anchor.end, margin)
            align(this, anchor, bias)
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
    }

    object Bottom : HorizontalAnchorEdge() {
        override fun ConstrainScope.linkToAnchor(
            anchor: ConstrainedLayoutReference,
            margin: Dp,
            bias: Float
        ) {
            top.linkTo(anchor.bottom, margin)
            align(this, anchor, bias)
        }

        override fun Path.drawTip(size: Size, layoutDirection: LayoutDirection) {
            moveTo(0f, size.height)
            lineTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
        }
    }
}