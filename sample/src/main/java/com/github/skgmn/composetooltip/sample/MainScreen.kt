package com.github.skgmn.composetooltip.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

private const val EXAMPLE_CONSTRAINT_LAYOUT = 0
private const val EXAMPLE_POPUP = 1

@Composable
fun MainScreen() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var exampleType by remember { mutableStateOf(EXAMPLE_CONSTRAINT_LAYOUT) }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (exampleType) {
                            EXAMPLE_CONSTRAINT_LAYOUT -> "Tooltip with ConstraintLayout"
                            EXAMPLE_POPUP -> "Tooltip as Popup"
                            else -> throw IllegalStateException()
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { toggleDrawer(scaffoldState, scope) }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                }
            )
        },
        drawerContent = {
            Text(
                text = "Tooltip with ConstraintLayout",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        toggleDrawer(scaffoldState, scope)
                        exampleType = EXAMPLE_CONSTRAINT_LAYOUT
                    }
                    .padding(16.dp)
            )
            Text(
                text = "Tooltip as Popup",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        toggleDrawer(scaffoldState, scope)
                        exampleType = EXAMPLE_POPUP
                    }
                    .padding(16.dp)
            )
        }
    ) {
        when (exampleType) {
            EXAMPLE_CONSTRAINT_LAYOUT -> ExampleConstraintLayout(contentPaddings = it)
            EXAMPLE_POPUP -> ExamplePopup(contentPaddings = it, scaffoldState = scaffoldState)
        }
    }
}

private fun toggleDrawer(
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
) {
    if (!scaffoldState.drawerState.isAnimationRunning) {
        scope.launch {
            if (scaffoldState.drawerState.isClosed) {
                scaffoldState.drawerState.open()
            } else {
                scaffoldState.drawerState.close()
            }
        }
    }
}