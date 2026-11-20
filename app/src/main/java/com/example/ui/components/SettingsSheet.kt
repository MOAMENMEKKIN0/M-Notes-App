package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppStrings
import com.example.model.LauncherIconSpec
import com.example.model.LibraryPhoto
import com.example.model.MNotesCatalog
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.model.TrimmedRingtone
import com.example.model.UserPreferences
import com.example.ui.MNotesViewModel
import com.example.ui.auth.completeEmailVerificationSignInLink
import com.example.ui.auth.getPendingSignInEmail
import com.example.ui.auth.onGoogleSignInClicked
import com.example.ui.auth.sendEmailVerificationSignInLink
import com.example.ui.auth.signOut
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesRefreshHz
import com.example.ui.theme.fontFamilyForId
import com.example.ui.theme.fontWeightForValue
import com.example.ui.theme.motionDurationMsForHz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    prefs: UserPreferences,
    tasks: List<SmartTask>,
    libraryPhotos: List<LibraryPhoto>,
    trimmedRingtones: List<TrimmedRingtone>,
    isBusyBackup: Boolean,
    viewModel: MNotesViewModel,
    initialFolder: String? = null,
    onPickBannerPhoto: () -> Unit,
    onAddLibraryPhotoUri: (Uri, String) -> Unit,
    onShowToast: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val lang = prefs.lang
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeDismisser = rememberSafeSheetDismisser(sheetState, onDismiss)
    val connectedEmail by viewModel.connectedEmail.collectAsStateWithLifecycle()
    val floatingBadges by viewModel.floatingBadges.collectAsStateWithLifecycle()
    val deletedFloatingBadges by viewModel.deletedFloatingBadges.collectAsStateWithLifecycle()
    val smartIconSettings by viewModel.smartIconSettings.collectAsStateWithLifecycle()
    val lastBackupStatus by viewModel.lastBackupStatus.collectAsStateWithLifecycle()
    val lastBackupError by viewModel.lastBackupError.collectAsStateWithLifecycle()

    var openFolder by remember(initialFolder) { mutableStateOf(initialFolder) }
    var openSubSection by remember { mutableStateOf<String?>(null) }
    var isLinkingGoogle by remember { mutableStateOf(false) }
    var googleLinkError by remember { mutableStateOf<String?>(null) }
    var showEmailLinkBox by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf(getPendingSignInEmail(context)) }
    var emailLinkSent by remember { mutableStateOf(false) }
    var isSendingEmailLink by remember { mutableStateOf(false) }
    var pasteLinkInput by remember { mutableStateOf("") }

    // Library upload name state
    var newPhotoName by remember { mutableStateOf("") }
    val libraryPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAddLibraryPhotoUri(uri, newPhotoName)
            newPhotoName = ""
        }
    }

    // JSON Export / Import via SAF
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val payload = viewModel.serializeBackupJson()
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(payload.toByteArray(Charsets.UTF_8))
                        } ?: error("Cannot open output stream")
                    }.isSuccess
                }
                if (ok) {
                    viewModel.playSound("done")
                    onShowToast(AppStrings.get(lang, "jsonExported"))
                } else {
                    onShowToast(AppStrings.get(lang, "invalidFile"))
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val raw = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { inp ->
                            inp.bufferedReader(Charsets.UTF_8).readText()
                        }
                    }.getOrNull()
                }
                if (raw.isNullOrBlank()) {
                    onShowToast(AppStrings.get(lang, "invalidFile"))
                } else {
                    viewModel.importBackupJson(raw)
                }
            }
        }
    }

    // Audio cutter state
    var cutterUri by remember { mutableStateOf<Uri?>(null) }
    var cutterClipName by remember { mutableStateOf("") }
    var cutterFileDurSec by remember { mutableIntStateOf(60) }
    var cutterStartSec by remember { mutableFloatStateOf(0f) }
    var cutterDurSec by remember { mutableFloatStateOf(10f) }
    var isTrimming by remember { mutableStateOf(false) }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            cutterUri = uri
            cutterStartSec = 0f
            cutterDurSec = 10f
            val fileName = runCatching {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                    if (c.moveToFirst()) c.getString(0) else null
                }
            }.getOrNull()
            cutterClipName = fileName?.substringBeforeLast('.')?.take(36)
                ?: AppStrings.get(lang, "defaultClipName")
            scope.launch {
                val dur = withContext(Dispatchers.IO) {
                    viewModel.soundEngine.getAudioDurationSeconds(uri)
                }
                cutterFileDurSec = dur.coerceAtLeast(10)
                if (cutterDurSec > cutterFileDurSec.toFloat()) {
                    cutterDurSec = cutterFileDurSec.coerceIn(3, 45).toFloat()
                }
            }
        }
    }

    // Banner text drafts with smooth debounced persistence
    var bannerTitleDraft by remember(prefs.bannerTitle) { mutableStateOf(prefs.bannerTitle) }
    var bannerSubDraft by remember(prefs.bannerSub) { mutableStateOf(prefs.bannerSub) }
    LaunchedEffect(bannerTitleDraft) {
        if (bannerTitleDraft != prefs.bannerTitle) {
            delay(400)
            viewModel.updatePreference(mapOf("bannerTitle" to bannerTitleDraft.take(100)))
        }
    }
    LaunchedEffect(bannerSubDraft) {
        if (bannerSubDraft != prefs.bannerSub) {
            delay(400)
            viewModel.updatePreference(mapOf("bannerSub" to bannerSubDraft.take(200)))
        }
    }

    ModalBottomSheet(
        onDismissRequest = { safeDismisser.onSystemDismiss() },
        sheetState = sheetState,
        containerColor = colors.bgMain,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get(lang, "settings"),
                    color = colors.textDark,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Button(
                    onClick = {
                        viewModel.playSound("done")
                        safeDismisser.dismiss()
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.testTag("settings_save_close")
                ) {
                    Text(
                        text = AppStrings.get(lang, "saveClose"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Online Backup & Sync
            SettingsFolder(
                title = if (lang == "en") "Backup & Sync" else "النسخ الاحتياطي والمزامنة السحابية",
                subtitle = if (lang == "en") "Cloud backup, auto sync, restore & passwordless email login" else "نسخ احتياطي سحابي تلقائي، استعادة ودخول بدون كلمة مرور",
                icon = Icons.Rounded.Backup,
                isOpen = openFolder == "backup",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "backup") null else "backup"
                }
            ) {
                val activeEmail = connectedEmail

                // Clear Backup Status Indicator Card (Status + Timestamp + Synced Item Counts)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when (lastBackupStatus) {
                        "success" -> Color(0xFFECFDF5)
                        "error" -> Color(0xFFFEF2F2)
                        else -> colors.bgMain
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = when (lastBackupStatus) {
                                "success" -> Color(0xFF6EE7B7)
                                "error" -> Color(0xFFFCA5A5)
                                else -> colors.borderSoft
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("backup_status_card")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (lastBackupStatus) {
                                "success" -> Icons.Rounded.CheckCircle
                                "error" -> Icons.Rounded.ErrorOutline
                                else -> Icons.Rounded.Backup
                            },
                            contentDescription = null,
                            tint = when (lastBackupStatus) {
                                "success" -> Color(0xFF059669)
                                "error" -> Color(0xFFDC2626)
                                else -> colors.primary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = when (lastBackupStatus) {
                                    "success" -> if (lang == "en") "Backup Succeeded" else "تم النسخ الاحتياطي بنجاح"
                                    "error" -> if (lang == "en") "Backup Failed" else "فشل النسخ الاحتياطي"
                                    else -> if (prefs.lastBackupLabel.isNotBlank()) {
                                        if (lang == "en") "Last Cloud Backup Saved" else "آخر نسخة سحابية محفوظة"
                                    } else {
                                        if (lang == "en") "No Cloud Backup Yet" else "لا توجد نسخة سحابية بعد"
                                    }
                                },
                                color = when (lastBackupStatus) {
                                    "success" -> Color(0xFF047857)
                                    "error" -> Color(0xFFB91C1C)
                                    else -> colors.textDark
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (lastBackupStatus == "error" && !lastBackupError.isNullOrBlank()) {
                                    lastBackupError!!
                                } else if (lang == "en") {
                                    "Last backup: ${prefs.lastBackupLabel.ifBlank { "Never" }} • ${tasks.size} tasks • ${floatingBadges.size} icons"
                                } else {
                                    "آخر نسخة: ${prefs.lastBackupLabel.ifBlank { "لا يوجد" }} • ${tasks.size} مهام • ${floatingBadges.size} أيقونات"
                                },
                                color = colors.textMuted,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (activeEmail.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.bgMain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (lang == "en") {
                                    "Passwordless Account Sign-In"
                                } else {
                                    "تسجيل الدخول بدون كلمة مرور (للحفظ والمزامنة)"
                                },
                                color = colors.textDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (lang == "en") {
                                    "Sign in securely using your Google account or an email verification link — no password needed. Your tasks, settings, and floating icon positions sync safely."
                                } else {
                                    "سجّل الدخول بحساب جوجل بضغطة واحدة أو عبر رابط تحقق يُرسل لبريدك بدون كلمة مرور، لحفظ مهامك وإعداداتك ومواقع الأيقونات العائمة."
                                },
                                color = colors.textMuted,
                                fontSize = 11.sp
                            )

                            if (!googleLinkError.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFEE2E2),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = googleLinkError!!,
                                        color = Color(0xFFB91C1C),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            // Primary 1-Tap Passwordless Google Email Sign-In
                            Button(
                                onClick = {
                                    googleLinkError = null
                                    isLinkingGoogle = true
                                    onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {
                                            isLinkingGoogle = false
                                            googleLinkError = null
                                            viewModel.playSound("done")
                                            onShowToast(
                                                if (lang == "en") "Signed in for Cloud Backup & Sync"
                                                else "تم تسجيل الدخول للنسخ الاحتياطي السحابي"
                                            )
                                        },
                                        onAuthError = { msg ->
                                            isLinkingGoogle = false
                                            googleLinkError = msg
                                        },
                                        scope = scope,
                                        onAuthCancelled = { detail ->
                                            isLinkingGoogle = false
                                            if (!detail.isNullOrBlank()) {
                                                googleLinkError = detail
                                            }
                                        }
                                    )
                                },
                                enabled = !isLinkingGoogle,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("settings_connect_google")
                            ) {
                                if (isLinkingGoogle) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Icon(Icons.Rounded.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == "en") "Sign In with Google (No Password)" else "الدخول السريع بحساب جوجل (بدون كلمة مرور)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Secondary Passwordless Option: Email Verification Link
                            OutlinedButton(
                                onClick = {
                                    viewModel.playSound("nav")
                                    showEmailLinkBox = !showEmailLinkBox
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("settings_toggle_email_link_signin")
                            ) {
                                Icon(Icons.Rounded.Email, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (lang == "en") "Sign In with Email Verification Link" else "تسجيل الدخول عبر رابط تحقق للبريد الإلكتروني",
                                    color = colors.primary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (showEmailLinkBox) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        singleLine = true,
                                        placeholder = {
                                            Text(
                                                text = if (lang == "en") "name@example.com" else "example@email.com",
                                                fontSize = 12.sp
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("settings_email_link_input")
                                    )

                                    Button(
                                        onClick = {
                                            googleLinkError = null
                                            isSendingEmailLink = true
                                            sendEmailVerificationSignInLink(
                                                context = context,
                                                rawEmail = emailInput,
                                                lang = lang,
                                                scope = scope,
                                                onLinkSent = {
                                                    isSendingEmailLink = false
                                                    emailLinkSent = true
                                                    viewModel.playSound("done")
                                                    onShowToast(
                                                        if (lang == "en") "Verification link sent! Check your inbox."
                                                        else "تم إرسال رابط التحقق إلى بريدك الإلكتروني!"
                                                    )
                                                },
                                                onError = { err ->
                                                    isSendingEmailLink = false
                                                    googleLinkError = err
                                                }
                                            )
                                        },
                                        enabled = !isSendingEmailLink,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .testTag("settings_send_email_link_btn")
                                    ) {
                                        if (isSendingEmailLink) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        } else {
                                            Text(
                                                text = if (lang == "en") "Send Verification Link" else "إرسال رابط التحقق",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (emailLinkSent) {
                                        Text(
                                            text = if (lang == "en") {
                                                "Tap the link in your email to finish signing in, or paste the verification link below:"
                                            } else {
                                                "اضغط على الرابط المرسل لبريدك لإكمال الدخول تلقائياً، أو الصق الرابط هنا:"
                                            },
                                            color = Color(0xFF047857),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        OutlinedTextField(
                                            value = pasteLinkInput,
                                            onValueChange = { pasteLinkInput = it },
                                            singleLine = true,
                                            placeholder = {
                                                Text(
                                                    text = if (lang == "en") "Paste email verification link..." else "الصق رابط التحقق هنا...",
                                                    fontSize = 11.5.sp
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                completeEmailVerificationSignInLink(
                                                    context = context,
                                                    emailLink = pasteLinkInput,
                                                    explicitEmail = emailInput,
                                                    lang = lang,
                                                    scope = scope,
                                                    onSuccess = { verified ->
                                                        googleLinkError = null
                                                        emailLinkSent = false
                                                        pasteLinkInput = ""
                                                        viewModel.playSound("done")
                                                        onShowToast(
                                                            if (lang == "en") "Signed in as $verified"
                                                            else "تم الدخول بحساب $verified"
                                                        )
                                                    },
                                                    onError = { err -> googleLinkError = err }
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (lang == "en") "Verify Link & Sign In" else "تأكيد الرابط وتسجيل الدخول",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.primarySoft,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${AppStrings.get(lang, "online")} $activeEmail",
                                        color = colors.textDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (lang == "en") {
                                            "Cloud sync active • All data & floating icons protected"
                                        } else {
                                            "المزامنة السحابية مفعلة • كل بياناتك وأيقوناتك محفوظة"
                                        },
                                        color = colors.textMuted,
                                        fontSize = 10.5.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.playSound("exit")
                                        signOut(credentialManager, scope)
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier.testTag("settings_sign_out")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.get(lang, "signOut"),
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    SettingsSwitchRow(
                        title = if (lang == "en") "Auto Backup (Sync changes automatically)" else "النسخ الاحتياطي التلقائي عند أي تعديل",
                        checked = prefs.autoBackup,
                        onCheckedChange = {
                            viewModel.playSound("nav")
                            viewModel.updatePreference(mapOf("autoBackup" to it))
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.runCloudBackup(isAuto = false) },
                        enabled = !isBusyBackup,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("settings_manual_backup")
                    ) {
                        if (isBusyBackup) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(Icons.Rounded.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (lang == "en") "Back Up Now" else "نسخ احتياطي الآن",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { viewModel.restoreLatestCloudBackup() },
                        enabled = !isBusyBackup,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("settings_restore_backup")
                    ) {
                        Icon(Icons.Rounded.Restore, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (lang == "en") "Restore Previous Backup" else "استعادة آخر نسخة احتياطية",
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            exportJsonLauncher.launch("m-notes-backup-${AppStrings.todayStr()}.json")
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("settings_export_json")
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(AppStrings.get(lang, "exportJson"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("settings_import_json")
                    ) {
                        Icon(Icons.Rounded.Upload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(AppStrings.get(lang, "importJson"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Language
            SettingsFolder(
                title = AppStrings.get(lang, "fLang"),
                subtitle = AppStrings.get(lang, "fLangSub"),
                icon = Icons.Rounded.Language,
                isOpen = openFolder == "lang",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "lang") null else "lang"
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSwitchRow(
                        title = "العربية (RTL)",
                        checked = prefs.lang == "ar",
                        onCheckedChange = {
                            viewModel.playSound("nav")
                            viewModel.updatePreference(mapOf("lang" to "ar"))
                        }
                    )
                    SettingsSwitchRow(
                        title = "English (LTR)",
                        checked = prefs.lang == "en",
                        onCheckedChange = {
                            viewModel.playSound("nav")
                            viewModel.updatePreference(mapOf("lang" to "en"))
                        }
                    )
                }
            }

            // 3. Images, Sizes & Shapes
            SettingsFolder(
                title = AppStrings.get(lang, "fMedia"),
                subtitle = AppStrings.get(lang, "fMediaSub"),
                icon = Icons.Rounded.Image,
                isOpen = openFolder == "media",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "media") null else "media"
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sub-section: Banner Photo
                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sBanPhoto"),
                        isOpen = openSubSection == "banPhoto",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "banPhoto") null else "banPhoto"
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MediaAvatarSlot(
                                imgUri = prefs.bannerImg,
                                size = bannerImageDp(prefs.bannerImgSize, prefs.bannerImgDp),
                                shapeId = prefs.bannerPhotoShape,
                                useBannerDefaultIfBlank = true,
                                contentDescription = prefs.bannerTitle
                            )
                            Button(
                                onClick = onPickBannerPhoto,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = AppStrings.get(lang, "pickImgBan"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        SizeControlSliderRow(
                            title = AppStrings.get(lang, "banSize"),
                            value = prefs.bannerImgDp.toFloat(),
                            valueRange = 28f..170f,
                            unitSuffix = "dp",
                            defaultValue = 54,
                            lang = lang,
                            testTag = "slider_banner_img_dp",
                            onValueCommitted = { viewModel.updatePreference(mapOf("bannerImgDp" to it)) }
                        )

                        Spacer(Modifier.height(10.dp))
                        SectionMiniLabel(AppStrings.get(lang, "banShape"))
                        ChoicePillRow(
                            options = listOf(
                                "ring" to AppStrings.get(lang, "shRing"),
                                "circle" to AppStrings.get(lang, "shCircle"),
                                "ios" to AppStrings.get(lang, "shIos"),
                                "blob" to AppStrings.get(lang, "shBlob")
                            ),
                            selectedId = prefs.bannerPhotoShape,
                            onSelect = {
                                viewModel.playSound("nav")
                                viewModel.updatePreference(mapOf("bannerPhotoShape" to it))
                            }
                        )
                    }

                    // Sub-section: Task Card Photo Size & Shape
                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sCardPhoto"),
                        isOpen = openSubSection == "cardPhoto",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "cardPhoto") null else "cardPhoto"
                        }
                    ) {
                        val demoTask = tasks.firstOrNull() ?: SmartTask(
                            id = "demo",
                            title = if (lang == "en") "Gym Workout" else "تمرين الجيم",
                            meta = AppStrings.formatTime("23:00", lang),
                            repeatMode = RepeatMode.CYCLE.wire,
                            cycleSteps = listOf("Legs", "Push", "Pull"),
                            alertTime = "23:00"
                        )
                        TaskCard(
                            task = demoTask,
                            lang = lang,
                            cardShape = prefs.cardShape,
                            taskImgSize = prefs.taskImgSize,
                            taskImgDp = prefs.taskImgDp,
                            isDemo = true
                        )

                        Spacer(Modifier.height(10.dp))
                        SizeControlSliderRow(
                            title = AppStrings.get(lang, "taskSize"),
                            value = prefs.taskImgDp.toFloat(),
                            valueRange = 28f..150f,
                            unitSuffix = "dp",
                            defaultValue = 48,
                            lang = lang,
                            testTag = "slider_task_img_dp",
                            onValueCommitted = { viewModel.updatePreference(mapOf("taskImgDp" to it)) }
                        )

                        Spacer(Modifier.height(8.dp))
                        SizeControlSliderRow(
                            title = if (lang == "en") "Card Height & Spacing" else "ارتفاع البطاقة والمسافات",
                            value = prefs.cardScalePercent.toFloat(),
                            valueRange = 80f..160f,
                            unitSuffix = "%",
                            defaultValue = 100,
                            lang = lang,
                            testTag = "slider_card_scale_pct",
                            onValueCommitted = { viewModel.updatePreference(mapOf("cardScalePercent" to it)) }
                        )

                        Spacer(Modifier.height(8.dp))
                        SizeControlSliderRow(
                            title = if (lang == "en") "Alert Photo Size" else "حجم الصورة في التنبيه",
                            value = prefs.alertImgDp.toFloat(),
                            valueRange = 44f..220f,
                            unitSuffix = "dp",
                            defaultValue = 120,
                            lang = lang,
                            testTag = "slider_alert_img_dp",
                            onValueCommitted = { viewModel.updatePreference(mapOf("alertImgDp" to it)) }
                        )

                        Spacer(Modifier.height(10.dp))
                        SectionMiniLabel(AppStrings.get(lang, "taskShape"))
                        ChoicePillRow(
                            options = listOf(
                                "ring" to AppStrings.get(lang, "shRing"),
                                "capsule" to AppStrings.get(lang, "shCap"),
                                "ios" to AppStrings.get(lang, "shIos"),
                                "blob" to AppStrings.get(lang, "shBlob"),
                                "strip" to AppStrings.get(lang, "shStrip")
                            ),
                            selectedId = prefs.cardShape,
                            onSelect = {
                                viewModel.playSound("nav")
                                viewModel.updatePreference(mapOf("cardShape" to it))
                            }
                        )
                    }

                    // Sub-section: App Photo Library
                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sLib"),
                        isOpen = openSubSection == "lib",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "lib") null else "lib"
                        }
                    ) {
                        OutlinedTextField(
                            value = newPhotoName,
                            onValueChange = { newPhotoName = it },
                            placeholder = { Text(AppStrings.get(lang, "imgNamePh"), fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.borderSoft
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                libraryPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(AppStrings.get(lang, "addLib"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(12.dp))
                        SectionMiniLabel(if (lang == "en") "Built-in Photo Folders" else "مجلدات صور البرنامج المصنفة")
                        CategorizedBuiltInPhotosBrowser(
                            lang = lang,
                            selectedUri = prefs.bannerImg,
                            columns = 3,
                            defaultCategory = "fruits",
                            showLabels = true,
                            onSelectUri = { uri ->
                                viewModel.playSound("done")
                                viewModel.updatePreference(mapOf("bannerImg" to uri))
                            }
                        )

                        if (libraryPhotos.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                libraryPhotos.forEach { photo ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = colors.bgMain,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            MediaAvatarSlot(
                                                imgUri = photo.filePath,
                                                size = 44.dp,
                                                shapeId = "circle",
                                                contentDescription = photo.name
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = photo.name,
                                                color = colors.textDark,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Switch(
                                                checked = photo.enabled,
                                                onCheckedChange = {
                                                    viewModel.toggleLibraryPhoto(photo.id, it)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedTrackColor = colors.primary,
                                                    checkedThumbColor = Color.White
                                                )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.removeLibraryPhoto(photo.id) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DeleteOutline,
                                                    contentDescription = "Delete",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Sounds, Gestures & Gym Beats
            SettingsFolder(
                title = AppStrings.get(lang, "fAudio"),
                subtitle = AppStrings.get(lang, "fAudioSub"),
                icon = Icons.Rounded.MusicNote,
                isOpen = openFolder == "audio",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "audio") null else "audio"
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Navigation Speed & Frame Rate (60 / 90 / 120 Hz)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.bgMain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (lang == "en") "Navigation Speed & Refresh Rate" else "سرعة التنقل واستجابة الشاشة (فريم/هرتز)",
                                color = colors.textDark,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (lang == "en") {
                                    "Choose between 60, 90, or 120 Hz for ultra-smooth transitions and touch response"
                                } else {
                                    "اختر بين ٦٠ أو ٩٠ أو ١٢٠ هرتز لسرعة التنقل والتفاعل مع الأزرار"
                                },
                                color = colors.textMuted,
                                fontSize = 10.5.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    60 to (if (lang == "en") "60 Hz" else "٦٠ هرتز\nعادي"),
                                    90 to (if (lang == "en") "90 Hz" else "٩٠ هرتز\nسريع"),
                                    120 to (if (lang == "en") "120 Hz" else "١٢٠ هرتز\nأقصى سرعة")
                                ).forEach { (hz, label) ->
                                    val selected = prefs.refreshRateHz == hz
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selected) colors.primary else colors.cardBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .border(
                                                1.5.dp,
                                                if (selected) colors.primary else colors.borderSoft,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                viewModel.playSound("nav")
                                                viewModel.updatePreference(mapOf("refreshRateHz" to hz))
                                            }
                                            .testTag("hz_${hz}_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = label,
                                                color = if (selected) Color.White else colors.textDark,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sRingtones"),
                        isOpen = openSubSection == "ringtones",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "ringtones") null else "ringtones"
                        }
                    ) {
                        SectionMiniLabel(AppStrings.get(lang, "gymGroup"))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MNotesCatalog.gymTracks.forEach { track ->
                                val active = prefs.activeRingId == track.id
                                SettingsPlaySwitchRow(
                                    title = if (lang == "en") track.nameEn else track.nameAr,
                                    playLabel = AppStrings.get(lang, "play"),
                                    checked = active,
                                    onPlay = { viewModel.playGymTrack(track.id) },
                                    onCheckedChange = {
                                        viewModel.playSound("nav")
                                        viewModel.updatePreference(mapOf("activeRingId" to track.id))
                                    }
                                )
                            }
                        }

                        if (trimmedRingtones.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            SectionMiniLabel(AppStrings.get(lang, "myRings"))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                trimmedRingtones.forEach { clip ->
                                    val active = prefs.activeRingId == clip.id
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (active) colors.primarySoft else colors.bgMain,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                if (active) colors.primary else colors.borderSoft,
                                                RoundedCornerShape(14.dp)
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = clip.name,
                                                    color = colors.textDark,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${clip.durationSec} ${AppStrings.get(lang, "sec")}",
                                                    color = colors.textMuted,
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                            MiniPlayChip(
                                                label = AppStrings.get(lang, "play"),
                                                onClick = { viewModel.playSavedRingtone(clip) }
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.deleteSavedRingtone(clip.id) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DeleteOutline,
                                                    contentDescription = "Delete",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Switch(
                                                checked = active,
                                                onCheckedChange = {
                                                    viewModel.playSound("nav")
                                                    viewModel.updatePreference(mapOf("activeRingId" to clip.id))
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedTrackColor = colors.primary,
                                                    checkedThumbColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { audioPicker.launch(arrayOf("audio/*")) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primarySoft, contentColor = colors.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.primary, RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.Rounded.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(AppStrings.get(lang, "cutBtn"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        val pickedAudio = cutterUri
                        if (pickedAudio != null) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.bgMain,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, colors.primary, RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = AppStrings.get(lang, "cutTool"),
                                        color = colors.primary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = cutterClipName,
                                        onValueChange = { cutterClipName = it },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    val maxStart = (cutterFileDurSec - 3).coerceAtLeast(1).toFloat()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${AppStrings.get(lang, "cutStart")}: ${AppStrings.formatSeconds(cutterStartSec.toInt())}",
                                            color = colors.textDark,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = AppStrings.formatSeconds(cutterFileDurSec),
                                            color = colors.textMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Slider(
                                        value = cutterStartSec.coerceIn(0f, maxStart),
                                        onValueChange = { cutterStartSec = it },
                                        valueRange = 0f..maxStart,
                                        colors = SliderDefaults.colors(
                                            thumbColor = colors.primary,
                                            activeTrackColor = colors.primary
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${AppStrings.get(lang, "cutDur")}: ${cutterDurSec.toInt()} ${AppStrings.get(lang, "sec")}",
                                            color = colors.textDark,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf(10, 15, 20, 30).forEach { sec ->
                                                val active = cutterDurSec.toInt() == sec
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(999.dp))
                                                        .background(if (active) colors.primary else colors.cardBg)
                                                        .border(1.dp, if (active) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                                                        .clickable { cutterDurSec = sec.toFloat() }
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = "$sec",
                                                        color = if (active) Color.White else colors.textMuted,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Slider(
                                        value = cutterDurSec,
                                        onValueChange = { cutterDurSec = it },
                                        valueRange = 3f..45f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = colors.primary,
                                            activeTrackColor = colors.primary
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.soundEngine.previewAudioFile(
                                                    pickedAudio,
                                                    cutterStartSec.toInt(),
                                                    cutterDurSec.toInt()
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(AppStrings.get(lang, "listen"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                if (isTrimming) return@Button
                                                isTrimming = true
                                                viewModel.trimAndSaveRingtone(
                                                    uri = pickedAudio,
                                                    clipName = cutterClipName,
                                                    startSec = cutterStartSec.toInt(),
                                                    durationSec = cutterDurSec.toInt()
                                                ) { ok ->
                                                    isTrimming = false
                                                    if (ok) cutterUri = null
                                                }
                                            },
                                            enabled = !isTrimming,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                            modifier = Modifier.weight(1.4f)
                                        ) {
                                            Text(
                                                text = if (isTrimming) AppStrings.get(lang, "cutting") else AppStrings.get(lang, "cutSave"),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sUiSounds"),
                        isOpen = openSubSection == "uiSounds",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "uiSounds") null else "uiSounds"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SettingsPlaySwitchRow(
                                title = AppStrings.get(lang, "sndNav"),
                                playLabel = AppStrings.get(lang, "tryNow"),
                                checked = prefs.soundNav,
                                onPlay = { viewModel.playSound("nav") },
                                onCheckedChange = { viewModel.updatePreference(mapOf("soundNav" to it)) }
                            )
                            SettingsPlaySwitchRow(
                                title = AppStrings.get(lang, "sndEnt"),
                                playLabel = AppStrings.get(lang, "tryNow"),
                                checked = prefs.soundEnter,
                                onPlay = { viewModel.playSound("enter") },
                                onCheckedChange = { viewModel.updatePreference(mapOf("soundEnter" to it)) }
                            )
                            SettingsPlaySwitchRow(
                                title = AppStrings.get(lang, "sndExt"),
                                playLabel = AppStrings.get(lang, "tryNow"),
                                checked = prefs.soundExit,
                                onPlay = { viewModel.playSound("exit") },
                                onCheckedChange = { viewModel.updatePreference(mapOf("soundExit" to it)) }
                            )
                            SettingsPlaySwitchRow(
                                title = AppStrings.get(lang, "sndDn"),
                                playLabel = AppStrings.get(lang, "tryNow"),
                                checked = prefs.soundDone,
                                onPlay = { viewModel.playSound("done") },
                                onCheckedChange = { viewModel.updatePreference(mapOf("soundDone" to it)) }
                            )
                        }
                    }

                    SettingsSubAccordion(
                        title = if (lang == "en") "Gestures & Haptics" else "الإيماءات والاهتزاز",
                        isOpen = openSubSection == "topbar",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "topbar") null else "topbar"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SettingsSwitchRow(
                                title = AppStrings.get(lang, "swCl"),
                                checked = prefs.swipeClose,
                                onCheckedChange = {
                                    viewModel.playSound("nav")
                                    viewModel.updatePreference(mapOf("swipeClose" to it))
                                }
                            )
                            SettingsSwitchRow(
                                title = AppStrings.get(lang, "hap"),
                                checked = prefs.hapticEnabled,
                                onCheckedChange = {
                                    viewModel.playSound("nav")
                                    viewModel.updatePreference(mapOf("hapticEnabled" to it))
                                }
                            )
                        }
                    }
                }
            }

            // 5. Fonts & Text Weight
            SettingsFolder(
                title = AppStrings.get(lang, "fFonts"),
                subtitle = AppStrings.get(lang, "fFontsSub"),
                icon = Icons.Rounded.TextFields,
                isOpen = openFolder == "fonts",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "fonts") null else "fonts"
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SizeControlSliderRow(
                        title = if (lang == "en") "Text Size Across App & Home" else "شريط تكبير وتصغير الكتابة في البرنامج",
                        value = prefs.textScalePercent.toFloat(),
                        valueRange = 75f..185f,
                        unitSuffix = "%",
                        defaultValue = 100,
                        lang = lang,
                        testTag = "slider_font_text_scale",
                        onValueCommitted = { viewModel.updatePreference(mapOf("textScalePercent" to it)) }
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.bgMain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = if (lang == "en") "Live Font Size Sample — M Notes" else "معاينة حية لحجم الكتابة الآن — مهامك اليومية",
                            color = colors.textDark,
                            fontSize = (13.5f * (prefs.textScalePercent / 100f)).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }

                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sWeight"),
                        isOpen = openSubSection == "weight",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "weight") null else "weight"
                        }
                    ) {
                        val weights = listOf(
                            400L to AppStrings.get(lang, "wThin"),
                            600L to AppStrings.get(lang, "wClear"),
                            800L to AppStrings.get(lang, "wBold")
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            weights.forEach { (w, label) ->
                                val active = prefs.fontWeight == w
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (active) colors.primarySoft else colors.bgMain,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (active) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
                                        .clickable {
                                            viewModel.playSound("nav")
                                            viewModel.updatePreference(mapOf("fontWeight" to w))
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            color = colors.textDark,
                                            fontSize = 13.sp,
                                            fontWeight = fontWeightForValue(w)
                                        )
                                        Switch(
                                            checked = active,
                                            onCheckedChange = {
                                                viewModel.playSound("nav")
                                                viewModel.updatePreference(mapOf("fontWeight" to w))
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedTrackColor = colors.primary,
                                                checkedThumbColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sFont"),
                        isOpen = openSubSection == "font",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "font") null else "font"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MNotesCatalog.fontOptions.forEach { opt ->
                                val active = prefs.fontId == opt.id
                                val fam = remember(opt.id) { fontFamilyForId(opt.id) }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (active) colors.primarySoft else colors.bgMain,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (active) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
                                        .clickable {
                                            viewModel.playSound("nav")
                                            viewModel.updatePreference(mapOf("fontId" to opt.id))
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = opt.nameEn,
                                                fontFamily = fam,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = colors.textDark
                                            )
                                            Text(
                                                text = "M Notes • مهامك اليومية",
                                                fontFamily = fam,
                                                fontSize = 11.sp,
                                                color = colors.textMuted
                                            )
                                        }
                                        Switch(
                                            checked = active,
                                            onCheckedChange = {
                                                viewModel.playSound("nav")
                                                viewModel.updatePreference(mapOf("fontId" to opt.id))
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedTrackColor = colors.primary,
                                                checkedThumbColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Tasks, Banner & Alerts
            SettingsFolder(
                title = AppStrings.get(lang, "fTasks"),
                subtitle = AppStrings.get(lang, "fTasksSub"),
                icon = Icons.Rounded.NotificationsActive,
                isOpen = openFolder == "tasks",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "tasks") null else "tasks"
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sAlerts"),
                        isOpen = openSubSection == "alerts",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "alerts") null else "alerts"
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BackgroundNotificationsHealthCard(
                                lang = lang,
                                onScheduleTestNotification = {
                                    viewModel.scheduleTestLockScreenNotification(context, delaySeconds = 5)
                                }
                            )
                            SizeControlSliderRow(
                                title = if (lang == "en") "Alert Avatar Photo Size" else "حجم الصورة داخل إشعار التنبيه",
                                value = prefs.alertImgDp.toFloat(),
                                valueRange = 44f..220f,
                                unitSuffix = "dp",
                                defaultValue = 120,
                                lang = lang,
                                testTag = "slider_alerts_tab_img_dp",
                                onValueCommitted = { viewModel.updatePreference(mapOf("alertImgDp" to it)) }
                            )
                            Spacer(Modifier.height(4.dp))
                            MNotesCatalog.alertStyles.forEach { opt ->
                                SettingsPlaySwitchRow(
                                    title = "${opt.id} • ${if (lang == "en") opt.nameEn else opt.nameAr}",
                                    playLabel = AppStrings.get(lang, "tryNow"),
                                    checked = prefs.alertStyle == opt.id,
                                    onPlay = { viewModel.previewAlertStyle(opt.id) },
                                    onCheckedChange = {
                                        viewModel.playSound("nav")
                                        viewModel.updatePreference(mapOf("alertStyle" to opt.id))
                                    }
                                )
                            }
                        }
                    }

                    SettingsSubAccordion(
                        title = if (lang == "en") {
                            "Reminder Screen Background & Doodles"
                        } else {
                            "خلفية وزخارف شاشة التذكير"
                        },
                        isOpen = openSubSection == "reminderBg",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "reminderBg") null else "reminderBg"
                        }
                    ) {
                        ReminderBackgroundStudio(
                            appliedThemeId = prefs.reminderBgThemeId,
                            appliedDoodlePackId = prefs.reminderDoodlePackId,
                            appliedDoodleOpacity = prefs.reminderDoodleOpacity,
                            alertStyle = prefs.alertStyle,
                            alertImgDp = prefs.alertImgDp,
                            sampleTask = tasks.firstOrNull(),
                            lang = lang,
                            onApply = { themeId, doodlePackId, opacity ->
                                viewModel.applyReminderBackground(themeId, doodlePackId, opacity)
                            }
                        )
                    }

                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sBanner"),
                        isOpen = openSubSection == "banner",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "banner") null else "banner"
                        }
                    ) {
                        HeroBanner(
                            prefs = prefs,
                            dueTodayCount = tasks.size,
                            isDemo = true
                        )
                        Spacer(Modifier.height(10.dp))
                        SettingsSwitchRow(
                            title = AppStrings.get(lang, "banShow"),
                            checked = prefs.showBanner,
                            onCheckedChange = {
                                viewModel.playSound("nav")
                                viewModel.updatePreference(mapOf("showBanner" to it))
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        SectionMiniLabel(AppStrings.get(lang, "banTxt"))
                        OutlinedTextField(
                            value = bannerTitleDraft,
                            onValueChange = { bannerTitleDraft = it },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = bannerSubDraft,
                            onValueChange = { bannerSubDraft = it },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        SectionMiniLabel(AppStrings.get(lang, "banIcon"))
                        ChoicePillRow(
                            options = MNotesCatalog.bannerIcons.map {
                                it.id to (if (lang == "en") it.nameEn else it.nameAr)
                            },
                            selectedId = prefs.bannerIcon,
                            onSelect = {
                                viewModel.playSound("nav")
                                viewModel.updatePreference(mapOf("bannerIcon" to it))
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                        SectionMiniLabel(AppStrings.get(lang, "banStyle"))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MNotesCatalog.bannerStyles.forEach { opt ->
                                SettingsSwitchRow(
                                    title = "${opt.id} • ${if (lang == "en") opt.nameEn else opt.nameAr}",
                                    checked = prefs.bannerStyle == opt.id,
                                    onCheckedChange = {
                                        viewModel.playSound("nav")
                                        viewModel.updatePreference(mapOf("bannerStyle" to opt.id))
                                    }
                                )
                            }
                        }
                    }

                    SettingsSubAccordion(
                        title = AppStrings.get(lang, "sRepeat"),
                        isOpen = openSubSection == "repeat",
                        onToggle = {
                            viewModel.playSound("nav")
                            openSubSection = if (openSubSection == "repeat") null else "repeat"
                        }
                    ) {
                        val today = AppStrings.todayStr()
                        val futureTasks = tasks.filter { it.nextDueDate.isNotBlank() && it.nextDueDate > today }
                        if (futureTasks.isEmpty()) {
                            Text(
                                text = AppStrings.get(lang, "noWait"),
                                color = colors.textMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                futureTasks.forEach { t ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = colors.bgMain,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = t.title,
                                                    color = colors.textDark,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                val cycleText = t.currentCycleLabel()
                                                Text(
                                                    text = listOf(t.nextDueDate, cycleText).filter { it.isNotBlank() }.joinToString(" • "),
                                                    color = colors.textMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            MiniPlayChip(
                                                label = AppStrings.get(lang, "simTom"),
                                                onClick = { viewModel.simulateTomorrow(t) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Themes & Colors
            SettingsFolder(
                title = AppStrings.get(lang, "fThemes"),
                subtitle = AppStrings.get(lang, "fThemesSub"),
                icon = Icons.Rounded.Brush,
                isOpen = openFolder == "themes",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "themes") null else "themes"
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.bgMain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = AppStrings.get(lang, "prevTheme"),
                                color = colors.textMuted,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (lang == "en") colors.nameEn else colors.nameAr,
                                color = colors.textDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.primary)
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = AppStrings.get(lang, "thmBtn"),
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MNotesCatalog.themes.forEach { themeSpec ->
                        val active = prefs.themeId == themeSpec.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (active) colors.primarySoft else colors.bgMain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (active) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.playSound("nav")
                                    viewModel.updatePreference(mapOf("themeId" to themeSpec.id))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        listOf(themeSpec.bgTopGlow, themeSpec.primary, themeSpec.accent).forEach { c ->
                                            Box(
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .clip(CircleShape)
                                                    .background(c)
                                                    .border(0.5.dp, Color(0x330F172A), CircleShape)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (lang == "en") themeSpec.nameEn else themeSpec.nameAr,
                                        color = colors.textDark,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Switch(
                                    checked = active,
                                    onCheckedChange = {
                                        viewModel.playSound("nav")
                                        viewModel.updatePreference(mapOf("themeId" to themeSpec.id))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = colors.primary,
                                        checkedThumbColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                InAppCustomColorStudio(
                    prefs = prefs,
                    lang = lang,
                    viewModel = viewModel
                )

                Spacer(Modifier.height(14.dp))
                SectionMiniLabel(AppStrings.get(lang, "banIcon"))
                ChoicePillRow(
                    options = MNotesCatalog.bannerIcons.map {
                        it.id to (if (lang == "en") it.nameEn else it.nameAr)
                    },
                    selectedId = prefs.bannerIcon,
                    onSelect = {
                        viewModel.playSound("nav")
                        viewModel.updatePreference(mapOf("bannerIcon" to it))
                    }
                )

                Spacer(Modifier.height(12.dp))
                SectionMiniLabel(AppStrings.get(lang, "banStyle"))
                ChoicePillRow(
                    options = MNotesCatalog.bannerStyles.map {
                        it.id to (if (lang == "en") it.nameEn else it.nameAr)
                    },
                    selectedId = prefs.bannerStyle,
                    onSelect = {
                        viewModel.playSound("nav")
                        viewModel.updatePreference(mapOf("bannerStyle" to it))
                    }
                )

                Spacer(Modifier.height(12.dp))
                SectionMiniLabel(AppStrings.get(lang, "banShape"))
                ChoicePillRow(
                    options = listOf(
                        "ring" to AppStrings.get(lang, "shRing"),
                        "circle" to AppStrings.get(lang, "shCircle"),
                        "ios" to AppStrings.get(lang, "shIos"),
                        "blob" to AppStrings.get(lang, "shBlob")
                    ),
                    selectedId = prefs.bannerPhotoShape,
                    onSelect = {
                        viewModel.playSound("nav")
                        viewModel.updatePreference(mapOf("bannerPhotoShape" to it))
                    }
                )

                Spacer(Modifier.height(12.dp))
                SectionMiniLabel(AppStrings.get(lang, "taskShape"))
                ChoicePillRow(
                    options = listOf(
                        "ring" to AppStrings.get(lang, "shRing"),
                        "capsule" to AppStrings.get(lang, "shCap"),
                        "ios" to AppStrings.get(lang, "shIos"),
                        "blob" to AppStrings.get(lang, "shBlob"),
                        "strip" to AppStrings.get(lang, "shStrip")
                    ),
                    selectedId = prefs.cardShape,
                    onSelect = {
                        viewModel.playSound("nav")
                        viewModel.updatePreference(mapOf("cardShape" to it))
                    }
                )
            }

            // 8A. Seamless Full-Screen Main Wallpaper Studio (Own Photo or Soft Pastel Wallpaper)
            SettingsFolder(
                title = if (lang == "en") {
                    "Main Screen Wallpaper"
                } else {
                    "خلفية الشاشة الرئيسية"
                },
                subtitle = if (lang == "en") {
                    "Choose your own photo or seamless full-screen wallpaper"
                } else {
                    "اختر صورتك الخاصة أو خلفية متصلة بالكامل بدون خط علوي"
                },
                icon = Icons.Rounded.Wallpaper,
                isOpen = openFolder == "mainWallpaper",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "mainWallpaper") null else "mainWallpaper"
                }
            ) {
                MainWallpaperStudio(
                    appliedWallpaperUri = prefs.mainWallpaperUri,
                    appliedDimPercent = prefs.mainWallpaperDimPercent,
                    lang = lang,
                    onPickDeviceImage = { uri -> viewModel.importWallpaperFromDevice(uri) },
                    onApply = { uri, dimPercent ->
                        viewModel.applyMainWallpaper(uri, dimPercent)
                    },
                    onReset = {
                        viewModel.resetMainWallpaper()
                    }
                )
            }

            // 8. Complete Home Screen Layout & Element Visibility Controls
            SettingsFolder(
                title = if (lang == "en") "Home Screen Layout & Elements" else "ضبط الواجهة الرئيسية بالكامل",
                subtitle = if (lang == "en") "Pick 1 of 3 layouts & hide or show any card on screen" else "اختيار التصميم وإخفاء أو إظهار أي جزء من الواجهة",
                icon = Icons.Rounded.DashboardCustomize,
                isOpen = openFolder == "homeLayout",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "homeLayout") null else "homeLayout"
                }
            ) {
                HomeScreenControlsContent(
                    prefs = prefs,
                    lang = lang,
                    viewModel = viewModel
                )
            }

            // 9. External Home-Screen App Icon Switcher & Color Studio (12 Samsung One UI Pixar 3D Icons)
            SettingsFolder(
                title = if (lang == "en") "App Icons & Custom Colors" else "أيقونة الهاتف الخارجية (١٢ أيقونة 3D بيكسار)",
                subtitle = if (lang == "en") "12 Samsung One UI squircle Pixar 3D character icons + custom" else "١٢ أيقونة بنمط سامسونج One UI وشخصيات 3D بيكسار + ألوان مخصصة",
                icon = Icons.Rounded.Apps,
                isOpen = openFolder == "appIcon",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "appIcon") null else "appIcon"
                }
            ) {
                ExternalAppIconFolderContent(
                    prefs = prefs,
                    lang = lang,
                    viewModel = viewModel
                )
            }

            // 10. Dedicated Floating Icons Settings Page (Manage, Show/Hide, Delete, Restore, Reset & 3D Library)
            SettingsFolder(
                title = if (lang == "en") "Floating Icons" else "الأيقونات العائمة",
                subtitle = if (lang == "en") "Show/hide, delete, restore deleted icons, reset positions & 3D library" else "إظهار وإخفاء وحذف واستعادة الأيقونات العائمة وإعادة تعيين المواقع",
                icon = Icons.Rounded.Mood,
                isOpen = openFolder == "characterBadge",
                lang = lang,
                onToggle = {
                    viewModel.playSound("nav")
                    openFolder = if (openFolder == "characterBadge") null else "characterBadge"
                }
            ) {
                FloatingBadgeSettingsFolderContent(
                    tasks = tasks,
                    smartIconSettings = smartIconSettings,
                    badges = floatingBadges,
                    deletedBadges = deletedFloatingBadges,
                    prefs = prefs,
                    lang = lang,
                    viewModel = viewModel,
                    onAppliedAndClose = { safeDismisser.dismiss() }
                )
            }

            // IP Seal
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.primarySoft,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(colors.primary, colors.accent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AppStrings.get(lang, "ipTitle"),
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = AppStrings.get(lang, "ipSub"),
                            color = colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsFolder(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isOpen: Boolean,
    lang: String,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = LocalMNotesColors.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isOpen) colors.primary else colors.borderSoft, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggle)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = colors.textDark,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = subtitle,
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.bgMain)
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isOpen) "▴" else AppStrings.get(lang, "tapOpen"),
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            val dur = motionDurationMsForHz(LocalMNotesRefreshHz.current)
            AnimatedVisibility(
                visible = isOpen,
                enter = expandVertically(tween(dur)) + fadeIn(tween(dur)),
                exit = shrinkVertically(tween(dur)) + fadeOut(tween(dur))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsSubAccordion(
    title: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = LocalMNotesColors.current
    val dur = motionDurationMsForHz(LocalMNotesRefreshHz.current)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isOpen) colors.primary else colors.borderSoft, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onToggle)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = colors.textDark,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(
                visible = isOpen,
                enter = expandVertically(tween(dur)) + fadeIn(tween(dur)),
                exit = shrinkVertically(tween(dur)) + fadeOut(tween(dur))
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalMNotesColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (checked) colors.primarySoft else colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (checked) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = colors.textDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.primary,
                    checkedThumbColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun SettingsPlaySwitchRow(
    title: String,
    playLabel: String,
    checked: Boolean,
    onPlay: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalMNotesColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (checked) colors.primarySoft else colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (checked) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = colors.textDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            MiniPlayChip(label = playLabel, onClick = onPlay)
            Spacer(Modifier.width(6.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.primary,
                    checkedThumbColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun MiniPlayChip(label: String, onClick: () -> Unit) {
    val colors = LocalMNotesColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.primarySoft)
            .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = label,
            tint = colors.primary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = label.replace("▶", "").trim(),
            color = colors.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionMiniLabel(text: String) {
    val colors = LocalMNotesColors.current
    Text(
        text = text,
        color = colors.textMuted,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoicePillRow(
    options: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    val colors = LocalMNotesColors.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (id, label) ->
            val active = selectedId == id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) colors.primary else colors.cardBg)
                    .border(
                        1.dp,
                        if (active) colors.primary else colors.borderSoft,
                        RoundedCornerShape(999.dp)
                    )
                    .clickable { onSelect(id) }
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    color = if (active) Color.White else colors.textMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
