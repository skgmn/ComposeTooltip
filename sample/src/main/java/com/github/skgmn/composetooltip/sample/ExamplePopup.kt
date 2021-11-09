package com.github.skgmn.composetooltip.sample

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.skgmn.composetooltip.*
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExamplePopup(
    contentPaddings: PaddingValues,
    scaffoldState: ScaffoldState
) {
    Box(
        modifier = Modifier
            .padding(contentPaddings)
            .fillMaxSize()
    ) {
        var anchorEdge by remember { mutableStateOf<AnchorEdge>(AnchorEdge.Top) }

        var imageOffsetX by remember { mutableStateOf(0.dp) }
        var imageOffsetY by remember { mutableStateOf(0.dp) }

        val tipPosition = remember { EdgePosition() }
        val anchorPosition = remember { EdgePosition() }

        var tooltipVisible by remember { mutableStateOf(false) }

        var tooltipStyle = rememberTooltipStyle(
            color = Color.LightGray,
            border = TooltipBorder(borderWidth = 2.dp, borderColor = Color.White),
            tipHeight = 14.dp
        )

        LaunchedEffect(Unit) {
            snapshotFlow { scaffoldState.drawerState.isClosed }
                .dropWhile { !it }
                .firstOrNull()
            tooltipVisible = true
        }

        BottomPanel(tipPosition, anchorPosition)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(imageOffsetX, imageOffsetY)
        ) {
            Text(
                text = "⬆",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
                    .buttonInvisible(tooltipVisible && anchorEdge == AnchorEdge.Top) {
                        anchorEdge = AnchorEdge.Top
                        tooltipVisible = true
                    }
                    .padding(8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⬅",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .buttonInvisible(tooltipVisible && anchorEdge == AnchorEdge.Start) {
                            anchorEdge = AnchorEdge.Start
                            tooltipVisible = true
                        }
                        .padding(8.dp)
                )
                val density = LocalDensity.current
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xffff0000))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consumeAllChanges()
                                with(density) {
                                    imageOffsetX += dragAmount.x.toDp()
                                    imageOffsetY += dragAmount.y.toDp()
                                }
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(R.drawable.dummy),
                        contentDescription = "dummy",
                    )
                    Tooltip(
                        anchorEdge = anchorEdge,
                        visible = tooltipVisible,
                        enterTransition = fadeIn(),
                        exitTransition = fadeOut(),
                        tipPosition = tipPosition,
                        anchorPosition = anchorPosition,
                        tooltipStyle = tooltipStyle,
                        modifier = Modifier.clickable(
                            remember { MutableInteractionSource() },
                            null
                        ) {
                            tooltipVisible = false
                        },
                        onDismissRequest = {
                            tooltipVisible = false
                        }
                    ) {
                        Text(
                            text = "Drag icon to move it.\nTouch tooltip to dismiss it.\nTouch arrows to move anchor edge.",
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                    }
                }
                Text(
                    text = "➡",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .buttonInvisible(tooltipVisible && anchorEdge == AnchorEdge.End) {
                            anchorEdge = AnchorEdge.End
                            tooltipVisible = true
                        }
                        .padding(8.dp)
                )
            }
            Text(
                text = "⬇",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
                    .buttonInvisible(tooltipVisible && anchorEdge == AnchorEdge.Bottom) {
                        anchorEdge = AnchorEdge.Bottom
                        tooltipVisible = true
                    }
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.BottomPanel(
    tipPosition: EdgePosition,
    anchorPosition: EdgePosition
) {
    val firstColumnWeight = 0.3f
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
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
                value = tipPosition.percent,
                onValueChange = { tipPosition.percent = it },
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
                value = anchorPosition.percent,
                onValueChange = { anchorPosition.percent = it },
                modifier = Modifier.weight(1f - firstColumnWeight)
            )
        }
    }
}

private fun Modifier.buttonInvisible(condition: Boolean, onClick: () -> Unit): Modifier {
    return if (condition) {
        alpha(0f)
    } else {
        clickable(onClick = onClick)
    }
}