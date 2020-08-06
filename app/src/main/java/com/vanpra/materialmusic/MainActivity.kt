package com.vanpra.materialmusic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.state
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.viewinterop.viewModel
import com.vanpra.materialmusic.layout.AppContent
import com.vanpra.materialmusic.layout.PlayerDrawer
import com.vanpra.materialmusic.layout.bottomNavHeight
import com.vanpra.materialmusic.layout.playerPeekHeight
import com.vanpra.materialmusic.theme.MaterialMusicTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
val ViewModelAmbient = ambientOf<MainViewModel> { error("No view Model") }

class MainActivity : AppCompatActivity() {
    @ExperimentalMaterialApi
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialMusicTheme {
                val mainViewModel = viewModel<MainViewModel>()

                Providers(ViewModelAmbient provides mainViewModel) {
                    val drawerRatio = state { 0f }
                    val peekRatio = state { 0f }
                    val drawerPadding = bottomNavHeight + (playerPeekHeight * peekRatio.value)

                    PlayerDrawer(drawerRatio = drawerRatio, peekRatio = peekRatio) {
                        Box(Modifier.fillMaxSize().padding(bottom = drawerPadding)) {
                            AppContent()
                        }
                    }
                }
            }
        }
    }
}