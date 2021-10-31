package com.vanpra.materialmusic.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.swipeable
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.vanpra.materialmusic.ui.MaterialMusicScaffold
import com.vanpra.materialmusic.viewmodels.MainViewModel
import com.vanpra.materialmusic.viewmodels.PlayerSwipeableState
import com.vanpra.materialmusic.viewmodels.PlayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed class Screen(val id: String) {
    object Home : Screen("home")
    object Songs : Screen("songs")
    object Playlist : Screen("playlist")
    object Stats : Screen("stats")
}

data class MenuItem(val name: String, val icon: ImageVector, val screen: Screen)

val items = listOf(
    MenuItem("Home", Icons.Default.Home, Screen.Home),
    MenuItem("Songs", Icons.Default.MusicNote, Screen.Songs),
    MenuItem("Playlist", Icons.Default.PlaylistPlay, Screen.Playlist),
    MenuItem("Stats", Icons.Default.BarChart, Screen.Stats),
)

@ExperimentalCoroutinesApi
@OptIn(
    ExperimentalMaterialNavigationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
)
@Composable
fun AppContent() {
    val navController = rememberNavController()
    val mainViewModel = viewModel<MainViewModel>()
    val playerViewModel = viewModel<PlayerViewModel>()

    MaterialMusicScaffold(
        topBar = {
            Spacer(
                Modifier
                    .statusBarsHeight()
                    .fillMaxWidth()
                    .offset()
            )
        },
        bottomBar = {
            Column {
                BottomNavBar(navController)
                Spacer(
                    Modifier
                        .navigationBarsHeight()
                        .fillMaxWidth()
                )
            }
        },
        player = {
            val playerPeekHeight = with(LocalDensity.current) { 56.dp.toPx() }
            val anchors = mapOf(
                0f to PlayerSwipeableState.Closed,
                playerPeekHeight to PlayerSwipeableState.Peek,
                it.toFloat() to PlayerSwipeableState.Expanded
            )
            val state by playerViewModel.state.collectAsState()

            Box(
                modifier = Modifier
                    .height(with(LocalDensity.current) { state.playerSwipeState.offset.value.toDp() })
                    .fillMaxWidth()
                    .background(Color.Red)
                    .swipeable(
                        state = state.playerSwipeState,
                        anchors = anchors,
                        orientation = Orientation.Vertical,
                        reverseDirection = true
                    )
            )
        }
    ) {
        NavHost(navController, startDestination = Screen.Home.id, modifier = Modifier.padding(it)) {
            composable(route = Screen.Home.id) {
                HomeLayout()
            }
            composable(route = Screen.Songs.id) {
                SongLayout(mainViewModel = mainViewModel, playerViewModel = playerViewModel)
            }
            composable(route = Screen.Playlist.id) {}
            composable(route = Screen.Stats.id) {}
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar {
        items.forEachIndexed { index, it ->
            NavigationBarItem(
                icon = { Icon(it.icon, null) },
                label = { Text(it.name) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(it.screen.id)
                }
            )
        }
    }
}
