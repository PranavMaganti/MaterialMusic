package com.vanpra.materialmusic.layout

import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.ExponentialDecay
import androidx.compose.animation.core.OnAnimationEnd
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TargetAnimation
import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.animation.FlingConfig
import androidx.compose.foundation.animation.fling
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.DpConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.offsetPx
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.onPreCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.composed
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFirstOrNull
import com.vanpra.materialmusic.ViewModelAmbient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.abs

enum class PlayerUIState {
    Opened,
    Closed,
    Peek
}

val playerPeekHeight = 64.dp

@ExperimentalCoroutinesApi
@Composable
fun PlayerDrawer(
    drawerRatio: MutableState<Float>,
    peekRatio: MutableState<Float>,
    mainContent: @Composable () -> Unit
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

@ExperimentalCoroutinesApi
fun Modifier.playerDraggable(
    minValue: Float,
    peekValue: Float,
    maxValue: Float,
    anchorsToState: List<Pair<Float, PlayerUIState>>,
    onNewValue: (Float) -> Unit
) = composed {
    val mainViewModel = ViewModelAmbient.current

    val currentState = mainViewModel.playerState.value
    val animationBuilder = SpringSpec<Float>(stiffness = 3500f)
    val forceAnimationCheck = state { true }
    val dragEnabled = state { true }
    val onAnimationEnd: OnAnimationEnd = { reason, endValue, _ ->
        if (reason != AnimationEndReason.Interrupted) {
            val newState = anchorsToState.firstOrNull { it.first == endValue }?.second
            if (newState != null && newState != currentState) {
                mainViewModel.playerState.value = newState
                forceAnimationCheck.value = !forceAnimationCheck.value
            }
        }
    }

    onPreCommit(currentState) {
        dragEnabled.value = true
    }

    val anchors = remember(anchorsToState) { anchorsToState.map { it.first } }
    val currentValue =
        anchorsToState.fastFirstOrNull { it.second == currentState }!!.first

    val flingConfig = FlingConfig(
        decayAnimation = ExponentialDecay(),
        adjustTarget = { target ->
        val point = anchors.minByOrNull { abs(it - target) }
        var adjusted = point ?: target
        val targetState = anchorsToState.fastFirstOrNull { it.first == adjusted }!!.second

        if (currentState == PlayerUIState.Opened && targetState == PlayerUIState.Closed) {
            adjusted = peekValue
        }

        TargetAnimation(adjusted, animationBuilder)
    })

    val clocks = AnimationClockAmbient.current
    val position = remember(clocks) {
        onNewValue(currentValue)
        CustomAnimatedFloat(
            currentValue,
            clocks,
            onNewValue,
            dragEnabled
        )
    }
    position.setBounds(minValue, maxValue)

    onCommit(currentValue, forceAnimationCheck.value) {
        position.animateTo(currentValue, animationBuilder)
    }

    Modifier.fillMaxSize().draggable(
        startDragImmediately = position.isRunning,
        orientation = Orientation.Vertical,
        onDragStopped = { position.fling(it, flingConfig, onAnimationEnd) },
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
fun PlayerContent(constraints: DpConstraints, offset: State<Float>) {
    Surface(modifier = Modifier.preferredSizeIn(constraints).offsetPx(y = offset)) {
        // Clickable used to block click events going to content behind the drawer
        val viewModel = ViewModelAmbient.current
        val song = viewModel.currentSong.collectAsState().value

        Box(
            Modifier.fillMaxSize().clickable(onClick = {
                if (viewModel.playerState.value == PlayerUIState.Peek) {
                    viewModel.playerState.value = PlayerUIState.Opened
                }
            }, indication = null),
            backgroundColor = Color.DarkGray
        ) {
            val artSize = playerPeekHeight - 16.dp
            Row(verticalGravity = Alignment.CenterVertically) {
                SongArt(song.art, Modifier.padding(8.dp).size(artSize))
                Column(Modifier.padding(8.dp)) {
                    Text(
                        song.name,
                        style = TextStyle(color = MaterialTheme.colors.onBackground, fontSize = 16.sp),
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = song.artist,
                        style = TextStyle(color = MaterialTheme.colors.onBackground, fontSize = 14.sp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}