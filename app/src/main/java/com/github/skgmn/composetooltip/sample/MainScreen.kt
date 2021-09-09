package com.github.skgmn.composetooltip.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.github.skgmn.composetooltip.AnchorEdge
import com.github.skgmn.composetooltip.Tooltip

@Composable
fun MainScreen() {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val anchorEdgeState = remember { mutableStateOf<AnchorEdge>(AnchorEdge.Top) }
        val anchorEdge by anchorEdgeState

        val anchor = createRef()
        val arrows = createRefs()

        Image(
            painter = painterResource(R.drawable.dummy),
            contentDescription = "dummy",
            modifier = Modifier
                .constrainAs(anchor) { centerTo(parent) }
                .size(64.dp)
                .background(Color(0xffff0000))
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
    val anchorEdge by anchorEdgeState
    if (anchorEdge != AnchorEdge.Start) {
        Text(
            text = "⬅",
            modifier = Modifier
                .constrainAs(refs.component1()) {
                    end.linkTo(anchor.start, 8.dp)
                    centerVerticallyTo(anchor)
                }
                .padding(4.dp)
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
                .padding(4.dp)
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
                .padding(4.dp)
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
                .padding(4.dp)
                .clickable { anchorEdgeState.value = AnchorEdge.Bottom }
        )
    }
}