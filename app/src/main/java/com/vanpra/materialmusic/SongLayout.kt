package com.vanpra.materialmusic


import android.net.Uri
import androidx.compose.Composable
import androidx.compose.collectAsState
import androidx.ui.animation.Crossfade
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.clip
import androidx.ui.foundation.Box
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.ImageAsset
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.MusicNote
import androidx.ui.text.TextStyle
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class SongData(
    val uri: Uri,
    val name: String,
    val artist: String,
    val duration: Int,
    val art: ImageAsset? = null
)

@ExperimentalCoroutinesApi
@Composable
fun SongLayout() {
    val mainViewModel = ViewModelAmbient.current

    val songs = mainViewModel.songs.collectAsState()
    Crossfade(current = songs.value.isEmpty()) { isEmpty ->
        when (isEmpty) {
            false ->
                LazyColumnItems(modifier = Modifier.fillMaxSize(), items = songs.value) {
                    SongItem(it) {
                        mainViewModel.currentSong.value = it
                        mainViewModel.playerState.value = PlayerUIState.Peek
                    }
                }
        }
    }
}

@Composable
fun SongItem(song: SongData, listener: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = { listener() })
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        SongArt(song.art, Modifier.padding(start = 8.dp, end = 8.dp).preferredSize(46.dp))
        Column(
            modifier = Modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                song.name,
                style = TextStyle(color = MaterialTheme.colors.onBackground, fontSize = 16.sp),
                maxLines = 1
            )
            Text(
                text = song.artist,
                style = TextStyle(color = MaterialTheme.colors.onBackground, fontSize = 14.sp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun SongArt(art: ImageAsset?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp)), gravity = Alignment.Center) {
        if (art != null) {
            Image(art, modifier = Modifier.wrapContentSize(Alignment.Center))
        } else {
            Image(
                Icons.Default.MusicNote,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground)
            )
        }
    }
}