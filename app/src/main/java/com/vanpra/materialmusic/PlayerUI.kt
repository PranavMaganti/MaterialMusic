package com.vanpra.materialmusic

import androidx.animation.AnimationEndReason
import androidx.animation.PhysicsBuilder
import androidx.animation.TargetAnimation
import androidx.compose.*
import androidx.compose.State
import androidx.ui.core.*
import androidx.ui.foundation.Box
import androidx.ui.foundation.Image
import androidx.ui.foundation.animation.FlingConfig
import androidx.ui.foundation.animation.fling
import androidx.ui.foundation.clickable
import androidx.ui.foundation.drawBackground
import androidx.ui.foundation.gestures.DragDirection
import androidx.ui.foundation.gestures.draggable
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.unit.dp
import androidx.ui.util.fastFirstOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.abs

enum class PlayerUIState {
    Opened,
    Closed,
    Peek
}

val playerPeekHeight = 56.dp

@ExperimentalCoroutinesApi
@Composable
fun PlayerDrawer(
    drawerRatio: MutableState<Float>,
    peekRatio: MutableState<Float>,
    mainContent: @Composable() () -> Unit
) {
    WithConstraints(Modifier.fillMaxSize()) {
        val minValue = 0f
        val maxValue = constraints.maxHeight.toFloat() - bottomNavHeight.toPx()
        val peekValue = maxValue - playerPeekHeight.toPx()

        val playerPx = playerPeekHeight.toPx()
        val dpConstraints = constraints.toDp()
        val offset = state { 0f }

        val anchors = listOf(
            maxValue to PlayerUIState.Closed,
            peekValue to PlayerUIState.Peek,
            minValue to PlayerUIState.Opened
        )

        Stack(Modifier.playerDraggable(minValue, peekValue, maxValue, anchors) {
            offset.value = it
            drawerRatio.value = (1 - (it / peekValue).coerceAtMost(1f))
            peekRatio.value = (1f - ((it - peekValue) / playerPx).coerceAtLeast(0f))
        }) {
            mainContent()
            PlayerContent(dpConstraints, offset)
            BottomNavBar(
                Modifier
                    .gravity(Alignment.BottomCenter)
                    .offset(y = bottomNavHeight * drawerRatio.value)
            )
        }
    }
}

//@ExperimentalCoroutinesApi
//@Composable
//fun PlayerDrawer2(
//    drawerRatio: MutableState<Float>,
//    peekRatio: MutableState<Float>,
//    mainContent: @Composable() () -> Unit
//) {
//    WithConstraints(Modifier.fillMaxSize()) {
//        val minValue = 0f
//        val maxValue = constraints.maxHeight.toFloat() - bottomNavHeight.toPx()
//        val peekValue = maxValue - playerPeekHeight.toPx()
//
//        val playerPx = playerPeekHeight.toPx()
//        val dpConstraints = constraints.toDp()
//        val offset = state { 0f }
//
//        val anchors = listOf(
//            maxValue to PlayerUIState.Closed,
//            peekValue to PlayerUIState.Peek,
//            minValue to PlayerUIState.Opened
//        )
//
//
//        ConstraintLayout(Modifier.playerDraggable(minValue, peekValue, maxValue, anchors) {
//            offset.value = it
//            drawerRatio.value = (1 - (it / peekValue).coerceAtMost(1f))
//            peekRatio.value = (1f - ((it - peekValue) / playerPx).coerceAtLeast(0f))
//        }) {
//            val (bottomBar, main, player) = createRefs()
//            Box(Modifier.constrainAs(main){
//
//            }) {
//                mainContent()
//            }
//
//            PlayerContent(dpConstraints, offset, Modifier.constrainAs(player){
//                bottom.linkTo(bottomBar.top)
//                end.linkTo(parent.end)
//                start.linkTo(parent.start)
//            })
//
//            BottomNavBar(
//                Modifier.constrainAs(bottomBar) {
//                    bottom.linkTo(parent.bottom)
//                    end.linkTo(parent.end)
//                    start.linkTo(parent.start)
//
//                    width = Dimension.fillToConstraints
//                }.height(bottomNavHeight *  (1f - drawerRatio.value))
//            )
//        }
//    }
//}
//

@ExperimentalCoroutinesApi
fun Modifier.playerDraggable(
    minValue: Float,
    peekValue: Float,
    maxValue: Float,
    anchorsToState: List<Pair<Float, PlayerUIState>>,
    onNewValue: (Float) -> Unit
) = composed {
    val mainViewModel = ViewModelAmbient.current

    val currentState = mainViewModel.playerState.collectAsState(PlayerUIState.Closed)
    val forceAnimationCheck = state { true }
    val animationBuilder = PhysicsBuilder<Float>(stiffness = 3500f)
    val dragEnabled = state { true }

    val anchors = remember(anchorsToState) { anchorsToState.map { it.first } }
    val currentValue =
        anchorsToState.fastFirstOrNull { it.second == currentState.value }!!.first

    val flingConfig = FlingConfig(adjustTarget = { target ->
        val point = anchors.minBy { abs(it - target) }
        var adjusted = point ?: target
        val targetState = anchorsToState.fastFirstOrNull { it.first == adjusted }!!.second

        if (currentState.value == PlayerUIState.Opened && targetState == PlayerUIState.Closed) {
            adjusted = peekValue
        }

        TargetAnimation(adjusted, animationBuilder)
    }, onAnimationEnd = { reason, endValue, _ ->
        if (reason != AnimationEndReason.Interrupted) {
            val newState = anchorsToState.firstOrNull { it.first == endValue }?.second
            if (newState != null && newState != currentState.value) {
                mainViewModel.playerState.value = newState
                forceAnimationCheck.value = !forceAnimationCheck.value
            }
        }
    })

    val clocks = AnimationClockAmbient.current

    val modifiedOnNewValue = { pos: Float ->
        if (dragEnabled.value) {
            onNewValue(pos)
            true
        } else {
            false
        }
    }

    val position = remember(clocks) {
        modifiedOnNewValue(currentValue)
        CustomAnimatedFloat(currentValue, clocks, modifiedOnNewValue)
    }
    position.setBounds(minValue, maxValue)

    onCommit(currentValue, forceAnimationCheck.value) {
        dragEnabled.value = true
        position.animateTo(currentValue, animationBuilder)
    }

    Modifier.fillMaxSize().draggable(
        startDragImmediately = position.isRunning,
        dragDirection = DragDirection.Vertical,
        onDragStopped = { position.fling(flingConfig, it) },
        onDragStarted = {
            dragEnabled.value = it.y < maxValue && it.y >= position.value
        }
    ) { delta ->
        position.snapTo(position.value + delta)
        delta
    }
}

@ExperimentalCoroutinesApi
@Composable
fun PlayerContent(constraints: DpConstraints, offset: State<Float>, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.preferredSizeIn(constraints).offsetPx(y = offset)) {
        // Clickable used to block click events going to content behind the drawer
        val viewModel = ViewModelAmbient.current

        ConstraintLayout(
            Modifier.fillMaxSize().drawBackground(Color.Red).clickable(onClick = {
                if (viewModel.playerState.value == PlayerUIState.Peek) {
                    viewModel.playerState.value = PlayerUIState.Opened
                }
            }, indication = null)) {

            val song = ViewModelAmbient.current.currentSong.value

            val (art, other) = createRefs()

            SongArt(art = song.art, modifier = Modifier.preferredSize(50.dp).constrainAs(art) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
            })
        }
    }
}