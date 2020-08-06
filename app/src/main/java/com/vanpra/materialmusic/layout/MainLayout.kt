package com.vanpra.materialmusic.layout

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.state
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.unit.dp
import com.vanpra.materialmusic.ViewModelAmbient
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
fun AppContent() {
    Crossfade(ViewModelAmbient.current.currentScreen.value) { screen ->
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
                modifier = Modifier.background(color = MaterialTheme.colors.background),
                icon = { Icon(it.icon) },
                selected = selectedItem.value == index,
                onSelect = {
                    selectedItem.value = index
                    mainViewModel.currentScreen.value = it.screen
                },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = Color.Gray
            )
        }
    }
}
