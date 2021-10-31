package com.vanpra.materialmusic.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

private enum class MaterialMusicScaffoldContent { TopBar, MainContent, Player, BottomBar }

@Composable
fun MaterialMusicScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    player: @Composable (layoutHeight: Int) -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    playerPeekHeight: Dp = 56.dp,
    content: @Composable (PaddingValues) -> Unit
) {
    val playerPeekHeightPx = with(LocalDensity.current) { playerPeekHeight.toPx() }

    Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
        SubcomposeLayout { constraints ->
            val layoutWidth = constraints.maxWidth
            val layoutHeight = constraints.maxHeight

            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            layout(layoutWidth, layoutHeight) {
                val topBarPlaceables = subcompose(MaterialMusicScaffoldContent.TopBar, topBar).map {
                    it.measure(looseConstraints)
                }

                val topBarHeight = topBarPlaceables.maxByOrNull { it.height }?.height ?: 0

                val bottomBarPlaceables = subcompose(MaterialMusicScaffoldContent.BottomBar) {
                    CompositionLocalProvider(
                        content = bottomBar
                    )
                }.map { it.measure(looseConstraints) }

                val playerPlaceables = subcompose(MaterialMusicScaffoldContent.Player) {
                    player(layoutHeight)
                }.map {
                    it.measure(looseConstraints)
                }

                val bottomBarHeight = bottomBarPlaceables.maxByOrNull { it.height }?.height ?: 0
                val playerHeight = playerPlaceables.maxByOrNull { it.height }?.height ?: 0
                val playerExpandedRatio =
                    ((playerHeight - playerPeekHeightPx) / (layoutHeight - playerPeekHeightPx))
                        .coerceAtLeast(0f)
                val bottomBarAdjustedHeight = (bottomBarHeight * (1 - playerExpandedRatio)).toInt()
                val bodyContentHeight = layoutHeight - topBarHeight

                val bodyContentPlaceables = subcompose(MaterialMusicScaffoldContent.MainContent) {
                    val innerPadding = PaddingValues(
                        bottom = bottomBarHeight.toDp() + min(
                            playerHeight.toDp(),
                            playerPeekHeight
                        )
                    )
                    content(innerPadding)
                }.map { it.measure(looseConstraints.copy(maxHeight = bodyContentHeight)) }

                bodyContentPlaceables.forEach {
                    it.place(0, topBarHeight)
                }
                topBarPlaceables.forEach {
                    it.place(0, 0)
                }
                bottomBarPlaceables.forEach {
                    it.place(0, layoutHeight - bottomBarAdjustedHeight)
                }
                playerPlaceables.forEach {
                    it.place(0, layoutHeight - bottomBarAdjustedHeight - playerHeight)
                }
            }
        }
    }
}