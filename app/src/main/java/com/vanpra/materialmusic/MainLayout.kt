package com.vanpra.materialmusic

import androidx.compose.Composable
import androidx.compose.collectAsState
import androidx.compose.state
import androidx.ui.animation.Crossfade
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Icon
import androidx.ui.foundation.drawBackground
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.material.BottomNavigation
import androidx.ui.material.BottomNavigationItem
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.BarChart
import androidx.ui.material.icons.filled.Home
import androidx.ui.material.icons.filled.MusicNote
import androidx.ui.material.icons.filled.PlaylistPlay
import androidx.ui.unit.dp
import androidx.ui.viewmodel.viewModel
import com.vanpra.materialmusic.ui.gray1000
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed class Screen {
    object Home : Screen()
    object Songs : Screen()
    object Playlist : Screen()
    object Stats : Screen()
}

data class MenuItem(val name: String, val icon: VectorAsset, val screen: Screen)

val items = listOf(
    MenuItem("Home", Icons.Default.Home, Screen.Home),
    MenuItem("Songs", Icons.Default.MusicNote, Screen.Songs),
    MenuItem("Playlist", Icons.Default.PlaylistPlay, Screen.Playlist),
    MenuItem("Stats", Icons.Default.BarChart, Screen.Stats)
)

val bottomNavHeight = 56.dp

@ExperimentalCoroutinesApi
@Composable
fun MainLayout() {
    val drawerRatio = state { 0f }
    val peekRatio = state { 0f }

    PlayerDrawer(drawerRatio = drawerRatio, peekRatio = peekRatio) {
        Box(
            Modifier
                .padding(bottom = bottomNavHeight + (playerPeekHeight * peekRatio.value))
                .fillMaxSize()
        ) {
            AppContent()
        }
    }
}

@ExperimentalCoroutinesApi
@Composable
private fun AppContent() {
    Crossfade(ViewModelAmbient.current.currentScreen.collectAsState().value) { screen ->
        Surface(color = MaterialTheme.colors.surface, modifier = Modifier.fillMaxSize()) {
            when (screen) {
                is Screen.Home -> HomeLayout()
                is Screen.Songs -> SongLayout()
                is Screen.Playlist -> HomeLayout()
                is Screen.Stats -> HomeLayout()
            }
        }
    }
}

@ExperimentalCoroutinesApi
@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    val selectedItem = state { 0 }
    val mainViewModel = ViewModelAmbient.current

    BottomNavigation(modifier) {
        items.forEachIndexed { index, it ->
            BottomNavigationItem(
                modifier = Modifier.drawBackground(MaterialTheme.colors.background),
                icon = { Icon(it.icon) },
                selected = selectedItem.value == index,
                onSelected = {
                    selectedItem.value = index
                    mainViewModel.currentScreen.value = it.screen
                },
                activeColor = MaterialTheme.colors.primaryVariant,
                inactiveColor = Color.Gray
            )
        }
    }
}
