package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.SoundEngine
import com.example.data.LocalMediaRepository
import com.example.data.LocalStoreRepository
import com.example.data.MNotesRepository
import com.example.ui.MNotesViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.MNotesTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MNotesAppRoot()
        }
    }
}

@Composable
private fun MNotesAppRoot() {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }

    val dbId = remember(appContext) { appContext.getString(R.string.firestore_database_id) }
    val db = remember(dbId) { FirebaseFirestore.getInstance(dbId) }
    val localStore = remember(appContext) { LocalStoreRepository(appContext) }
    val cloudRepo = remember(db) { MNotesRepository(db) }
    val mediaRepo = remember(appContext) { LocalMediaRepository(appContext) }
    val soundEngine = remember(appContext) { SoundEngine(appContext) }

    val factory = remember(localStore, cloudRepo, mediaRepo, soundEngine) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MNotesViewModel(
                    localStore = localStore,
                    repository = cloudRepo,
                    mediaRepository = mediaRepo,
                    soundEngine = soundEngine
                ) as T
            }
        }
    }
    val vm: MNotesViewModel = viewModel(factory = factory)
    val prefs by vm.preferences.collectAsStateWithLifecycle()

    MNotesTheme(
        themeId = prefs.themeId,
        fontId = prefs.fontId,
        fontWeightValue = prefs.fontWeight
    ) {
        MainScreen(viewModel = vm)
    }
}
