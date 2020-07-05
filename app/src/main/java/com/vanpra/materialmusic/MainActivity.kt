package com.vanpra.materialmusic

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Providers
import androidx.compose.ambientOf
import androidx.compose.launchInComposition
import androidx.ui.core.setContent
import androidx.ui.viewmodel.viewModel
import com.vanpra.materialmusic.ui.MaterialMusicTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
val ViewModelAmbient = ambientOf<MainViewModel> { error("No view Model") }

class MainActivity : AppCompatActivity() {
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialMusicTheme {
                val mainViewModel = viewModel<MainViewModel>()

                launchInComposition {
                    mainViewModel.getSongs()
                }

                Providers(ViewModelAmbient provides mainViewModel) {
                    MainLayout()
                }
            }
        }
    }
}