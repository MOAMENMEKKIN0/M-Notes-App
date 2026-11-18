package com.example

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MNotesAppRoot()
        }
    }
}

private fun applyPreferredRefreshRate(activity: Activity?, targetHz: Int) {
    val window = activity?.window ?: return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    runCatching {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay
        } ?: return

        @Suppress("DEPRECATION")
        val currentMode = display.mode
        val sameResModes = display.supportedModes.filter {
            it.physicalWidth == currentMode.physicalWidth &&
                it.physicalHeight == currentMode.physicalHeight
        }
        val pool = if (sameResModes.isNotEmpty()) sameResModes else display.supportedModes.toList()
        val desiredF = targetHz.toFloat()
        val bestMode = pool.minByOrNull { abs(it.refreshRate - desiredF) } ?: return
        val attrs = window.attributes
        if (attrs.preferredDisplayModeId != bestMode.modeId) {
            attrs.preferredDisplayModeId = bestMode.modeId
            window.attributes = attrs
        }
    }
}

@Composable
private fun MNotesAppRoot() {
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }
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

    LaunchedEffect(prefs.refreshRateHz) {
        applyPreferredRefreshRate(activity, prefs.refreshRateHz)
    }

    MNotesTheme(
        themeId = prefs.themeId,
        fontId = prefs.fontId,
        fontWeightValue = prefs.fontWeight,
        refreshRateHz = prefs.refreshRateHz,
        customPrimaryHex = prefs.customPrimaryHex,
        customBgHex = prefs.customBgHex,
        customAccentHex = prefs.customAccentHex
    ) {
        MainScreen(viewModel = vm)
    }
}
