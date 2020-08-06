package com.vanpra.materialmusic.layout

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanpra.materialmusic.ViewModelAmbient
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
    Crossfade(current = songs.value.isEmpty(), modifier = Modifier.padding(top = 8.dp)) { isEmpty ->
        when (isEmpty) {
            false ->
                LazyColumnFor(
                    items = songs.value,
                    modifier = Modifier.fillMaxSize(),
                    itemContent = {
                        SongItem(it) {
                            mainViewModel.currentSong.value = it
                            mainViewModel.playerState.value = PlayerUIState.Peek
                        }
                    })
        }
    }
}

@Composable
fun SongItem(song: SongData, listener: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = { listener() })
            .padding(top = 8.dp, bottom = 8.dp),
        verticalGravity = Alignment.CenterVertically
    ) {
        SongArt(song.art, Modifier.padding(start = 8.dp, end = 8.dp).preferredSize(50.dp))
        Column(
            modifier = Modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                song.name,
                style = TextStyle(color = MaterialTheme.colors.onBackground, fontSize = 18.sp),
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

@Composable
fun SongArt(art: ImageAsset?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp)), gravity = Alignment.Center) {
        if (art != null) {
            Image(art, modifier = Modifier.wrapContentSize(Alignment.Center))
        } else {
            Image(
                Icons.Default.MusicNote,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground),
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )
        }
    }
}