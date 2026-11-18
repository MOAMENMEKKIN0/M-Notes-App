package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.SoundEngine
import com.example.data.LocalMediaRepository
import com.example.data.MNotesRepository
import com.example.ui.MNotesViewModel
import com.example.ui.MainScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.MNotesTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MNotesAppRoot()
        }
    }
}

private fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val auth = Firebase.auth
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
}

@Composable
private fun MNotesAppRoot() {
    val context = LocalContext.current
    val currentUser by authStateFlow().collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)
    var unauthLang by remember { mutableStateOf("ar") }

    val dbId = remember(context) { context.getString(R.string.firestore_database_id) }
    val db = remember(dbId) { FirebaseFirestore.getInstance(dbId) }
    val repository = remember(db) { MNotesRepository(db) }
    val mediaRepo = remember(context) { LocalMediaRepository(context.applicationContext) }
    val soundEngine = remember(context) { SoundEngine(context.applicationContext) }

    val user = currentUser
    if (user == null) {
        MNotesTheme(
            themeId = "mint-clean",
            fontId = "cairo",
            fontWeightValue = 600L
        ) {
            AuthScreen(
                lang = unauthLang,
                onToggleLang = { unauthLang = it },
                onAuthSuccess = {}
            )
        }
    } else {
        key(user.uid) {
            val factory = remember(user.uid, repository, mediaRepo, soundEngine) {
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MNotesViewModel(
                            repository = repository,
                            mediaRepository = mediaRepo,
                            soundEngine = soundEngine,
                            currentUserId = user.uid,
                            userEmail = user.email.orEmpty()
                        ) as T
                    }
                }
            }
            val vm: MNotesViewModel = viewModel(key = user.uid, factory = factory)
            val prefs by vm.preferences.collectAsStateWithLifecycle()

            MNotesTheme(
                themeId = prefs.themeId,
                fontId = prefs.fontId,
                fontWeightValue = prefs.fontWeight
            ) {
                MainScreen(viewModel = vm)
            }
        }
    }
}
