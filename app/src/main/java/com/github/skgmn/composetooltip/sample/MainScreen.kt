package com.github.skgmn.composetooltip.sample

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.github.skgmn.composetooltip.AnchorEdge
import com.github.skgmn.composetooltip.Tooltip

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { screenSize = it }
    ) {
        val anchorEdgeState = remember { mutableStateOf<AnchorEdge>(AnchorEdge.Top) }

        var imageBiasX by remember { mutableStateOf(0.5f) }
        var imageBiasY by remember { mutableStateOf(0.5f) }

        val tipPositionState = remember { mutableStateOf(0.5f) }
        val anchorPositionState = remember { mutableStateOf(0.5f) }

        val tooltipVisibleState = remember { mutableStateOf(true) }

        val anchor = createRef()
        val arrows = createRefs()
        val bottomPanel = createRef()

        BottomPanel(bottomPanel, tipPositionState, anchorPositionState)

        Image(
            painter = painterResource(R.drawable.dummy),
            contentDescription = "dummy",
            modifier = Modifier
                .constrainAs(anchor) {
                    linkTo(
                        parent.start, parent.top, parent.end, parent.bottom,
                        horizontalBias = imageBiasX,
                        verticalBias = imageBiasY
                    )
                }
                .size(64.dp)
                .background(Color(0xffff0000))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consumeAllChanges()
                        imageBiasX += dragAmount.x / screenSize.width
                        imageBiasY += dragAmount.y / screenSize.height
                    }
                }
        )

        AnchorChangeButtons(anchorEdgeState, tooltipVisibleState, arrows, anchor)

        Tooltip(
            anchor = anchor,
            anchorEdge = anchorEdgeState.value,
            visible = tooltipVisibleState.value,
            enterTransition = fadeIn(),
            exitTransition = fadeOut(),
            tipPosition = tipPositionState.value,
            anchorPosition = anchorPositionState.value,
            modifier = Modifier
                .clickable(remember { MutableInteractionSource() }, null) {
                    tooltipVisibleState.value = false
                }
        ) {
            Text(
                text = "Drag icon to move it.\nTouch tooltip to dismiss it.\nTouch arrows to move anchor edge.",
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
    }
}

@Composable
private fun ConstraintLayoutScope.BottomPanel(
    ref: ConstrainedLayoutReference,
    tipPositionState: MutableState<Float>,
    anchorPositionState: MutableState<Float>
) {
    val firstColumnWeight = 0.3f
    Column(
        modifier = Modifier
            .constrainAs(ref) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tip position",
                modifier = Modifier.weight(firstColumnWeight)
            )
            Slider(
                value = tipPositionState.value,
                onValueChange = { tipPositionState.value = it },
                modifier = Modifier.weight(1f - firstColumnWeight)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Anchor position",
                modifier = Modifier.weight(firstColumnWeight)
            )
            Slider(
                value = anchorPositionState.value,
                onValueChange = { anchorPositionState.value = it },
                modifier = Modifier.weight(1f - firstColumnWeight)
            )
        }
    }
}

@Composable
private fun ConstraintLayoutScope.AnchorChangeButtons(
    anchorEdgeState: MutableState<AnchorEdge>,
    tooltipVisibleState: MutableState<Boolean>,
    refs: ConstraintLayoutScope.ConstrainedLayoutReferences,
    anchor: ConstrainedLayoutReference
) {
    val padding = 8.dp
    val anchorEdge by anchorEdgeState
    if (!tooltipVisibleState.value || anchorEdge != AnchorEdge.Start) {
        Text(
            text = "⬅",
            modifier = Modifier
                .constrainAs(refs.component1()) {
                    end.linkTo(anchor.start, 8.dp)
                    centerVerticallyTo(anchor)
                }
                .clickable {
                    anchorEdgeState.value = AnchorEdge.Start
                    tooltipVisibleState.value = true
                }
                .padding(padding)
        )
    }
    if (!tooltipVisibleState.value || anchorEdge != AnchorEdge.Top) {
        Text(
            text = "⬆",
            modifier = Modifier
                .constrainAs(refs.component2()) {
                    bottom.linkTo(anchor.top, 8.dp)
                    centerHorizontallyTo(anchor)
                }
                .clickable {
                    anchorEdgeState.value = AnchorEdge.Top
                    tooltipVisibleState.value = true
                }
                .padding(padding)
        )
    }
    if (!tooltipVisibleState.value || anchorEdge != AnchorEdge.End) {
        Text(
            text = "➡",
            modifier = Modifier
                .constrainAs(refs.component3()) {
                    start.linkTo(anchor.end, 8.dp)
                    centerVerticallyTo(anchor)
                }
                .clickable {
                    anchorEdgeState.value = AnchorEdge.End
                    tooltipVisibleState.value = true
                }
                .padding(padding)
        )
    }
    if (!tooltipVisibleState.value || anchorEdge != AnchorEdge.Bottom) {
        Text(
            text = "⬇",
            modifier = Modifier
                .constrainAs(refs.component4()) {
                    top.linkTo(anchor.bottom, 8.dp)
                    centerHorizontallyTo(anchor)
                }
                .clickable {
                    anchorEdgeState.value = AnchorEdge.Bottom
                    tooltipVisibleState.value = true
                }
                .padding(padding)
        )
    }
}