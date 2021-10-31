package com.vanpra.materialmusic.layout

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanpra.materialmusic.viewmodels.MainViewModel
import com.vanpra.materialmusic.viewmodels.PlayerSwipeableState
import com.vanpra.materialmusic.viewmodels.PlayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

data class Song(
    val uri: Uri,
    val name: String,
    val artist: String,
    val duration: Int,
    val art: ImageBitmap? = null
)

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalCoroutinesApi
@Composable
fun SongLayout(mainViewModel: MainViewModel, playerViewModel: PlayerViewModel) {
    val mainViewModelState by mainViewModel.state.collectAsState()
    val playerViewModelState by playerViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        Text(
            text = "Songs",
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp),
            style = MaterialTheme.typography.headlineMedium
        )

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            items(items = mainViewModelState.songs) {
                SongItem(it) {
                    playerViewModel.state.value = playerViewModel.state.value.copy(playing = it)
                    coroutineScope.launch {
                        playerViewModelState.playerSwipeState.animateTo(PlayerSwipeableState.Peek)
                    }
                }
            }
        }
    }
}

@Composable
fun SongItem(song: Song?, listener: () -> Unit = {}) {
    if (song != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { listener() })
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongArt(
                song.art,
                Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(50.dp)
            )
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    song.name,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = song.artist,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SongArt(art: ImageBitmap?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
        if (art != null) {
            Image(
                art,
                contentDescription = null,
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )
        } else {
            Image(
                Icons.Default.MusicNote,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )
        }
    }
}