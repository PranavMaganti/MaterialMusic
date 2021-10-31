package com.vanpra.materialmusic.viewmodels

import android.app.Application
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.lifecycle.AndroidViewModel
import com.vanpra.materialmusic.layout.Song
import kotlinx.coroutines.flow.MutableStateFlow

enum class PlayerSwipeableState {
    Closed,
    Peek,
    Expanded
}

data class PlayerViewModelState @OptIn(ExperimentalMaterialApi::class) constructor(
    val playing: Song? = null,
    val playerSwipeState: SwipeableState<PlayerSwipeableState> = SwipeableState(PlayerSwipeableState.Closed)
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    val state = MutableStateFlow(PlayerViewModelState())
}