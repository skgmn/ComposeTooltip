package com.github.skgmn.composetooltip.sample

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
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

@Composable
fun MainScreen() {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { screenSize = it }
    ) {
        val anchorEdgeState = remember { mutableStateOf<AnchorEdge>(AnchorEdge.Top) }
        val anchorEdge by anchorEdgeState

        var imageBiasX by remember { mutableStateOf(0.5f) }
        var imageBiasY by remember { mutableStateOf(0.5f) }

        val anchor = createRef()
        val arrows = createRefs()

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

        AnchorChangeButtons(anchorEdgeState, arrows, anchor)

        Tooltip(anchor = anchor, anchorEdge = anchorEdge) {
            Text(text = "Drag image 😄")
        }
    }
}

@Composable
private fun ConstraintLayoutScope.AnchorChangeButtons(
    anchorEdgeState: MutableState<AnchorEdge>,
    refs: ConstraintLayoutScope.ConstrainedLayoutReferences,
    anchor: ConstrainedLayoutReference
) {
    val padding = 8.dp
    val anchorEdge by anchorEdgeState
    if (anchorEdge != AnchorEdge.Start) {
        Text(
            text = "⬅",
            modifier = Modifier
                .constrainAs(refs.component1()) {
                    end.linkTo(anchor.start, 8.dp)
                    centerVerticallyTo(anchor)
                }
                .padding(padding)
                .clickable { anchorEdgeState.value = AnchorEdge.Start }
        )
    }
    if (anchorEdge != AnchorEdge.Top) {
        Text(
            text = "⬆",
            modifier = Modifier
                .constrainAs(refs.component2()) {
                    bottom.linkTo(anchor.top, 8.dp)
                    centerHorizontallyTo(anchor)
                }
                .padding(padding)
                .clickable { anchorEdgeState.value = AnchorEdge.Top }
        )
    }
    if (anchorEdge != AnchorEdge.End) {
        Text(
            text = "➡",
            modifier = Modifier
                .constrainAs(refs.component3()) {
                    start.linkTo(anchor.end, 8.dp)
                    centerVerticallyTo(anchor)
                }
                .padding(padding)
                .clickable { anchorEdgeState.value = AnchorEdge.End }
        )
    }
    if (anchorEdge != AnchorEdge.Bottom) {
        Text(
            text = "⬇",
            modifier = Modifier
                .constrainAs(refs.component4()) {
                    top.linkTo(anchor.bottom, 8.dp)
                    centerHorizontallyTo(anchor)
                }
                .padding(padding)
                .clickable { anchorEdgeState.value = AnchorEdge.Bottom }
        )
    }
}