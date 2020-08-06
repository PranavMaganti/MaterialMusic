package com.vanpra.materialmusic

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageAsset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vanpra.materialmusic.layout.PlayerUIState
import com.vanpra.materialmusic.layout.Screen
import com.vanpra.materialmusic.layout.SongData
import com.vanpra.materialmusic.layout.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MediaColumns(val id: Int, val name: Int, val artist: Int, val duration: Int)

@ExperimentalCoroutinesApi
class MainViewModel(application: Application) : AndroidViewModel(application) {
    val playerState = mutableStateOf(PlayerUIState.Closed)
    val currentScreen: MutableState<Screen> = mutableStateOf(Screen.Home)
    val currentSong = MutableStateFlow(SongData(Uri.parse(""), "", "", 0))
    val songs = MutableStateFlow<List<SongData>>(listOf())

    private val whatsappPattern = Regex("AUD-.*-WA.*")

    init {
        viewModelScope.launch {
            getSongs()
        }
    }

    private suspend fun getSongs() {
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
            )
            val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

            val cursor = getApplication<Application>().contentResolver?.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
            ) ?: throw IllegalArgumentException()

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val columns = MediaColumns(idColumn, nameColumn, artistColumn, durationColumn)

            songs.value = cursor.map { parseToSong(it, columns) }
            cursor.close()
        }
    }

    private fun parseToSong(cursor: Cursor, columns: MediaColumns): SongData? {
        val songUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            cursor.getLong(columns.id)
        )
        val name = cursor.getString(columns.name)

        if (whatsappPattern.matches(name)) {
            return null
        }

        val artist = cursor.getString(columns.artist)
        val duration = cursor.getInt(columns.duration)

        val mmr = MediaMetadataRetriever()
        val bfo = BitmapFactory.Options()

        return try {
            mmr.setDataSource(getApplication(), songUri)
            val rawArt = mmr.embeddedPicture

            val art = if (rawArt != null) BitmapFactory.decodeByteArray(
                rawArt,
                0,
                rawArt.size,
                bfo
            ).asImageAsset() else null

            SongData(songUri, name, artist, duration, art)
        } catch (e: RuntimeException) {
            null
        }
    }
}




