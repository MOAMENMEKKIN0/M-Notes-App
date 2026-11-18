package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.localization.AppStrings
import com.example.ui.theme.LocalMNotesColors
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    scope.launch {
        try {
            val cred = credentialManager.getCredential(context, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: (String?) -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()
    scope.launch {
        try {
            val activity = context as? Activity
                ?: throw IllegalStateException("Activity context required for Credential Manager")
            val cred = credentialManager.getCredential(activity, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled(e.message)
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

@Composable
fun AuthScreen(
    lang: String,
    onToggleLang: (String) -> Unit,
    onAuthSuccess: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isGoogleLoading by remember { mutableStateOf(false) }
    var isPhoneSending by remember { mutableStateOf(false) }
    var isOtpVerifying by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var infoMsg by remember { mutableStateOf<String?>(null) }

    var phoneInput by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    fun startPhoneVerification(useResendToken: Boolean) {
        val cleaned = phoneInput.trim().replace(" ", "")
        if (!cleaned.startsWith("+") || cleaned.length < 8) {
            errorMsg = AppStrings.get(lang, "phoneInvalidError")
            infoMsg = null
            return
        }
        val activity = context as? Activity
        if (activity == null) {
            errorMsg = "Activity context required"
            return
        }
        errorMsg = null
        infoMsg = null
        isPhoneSending = true

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                scope.launch {
                    try {
                        Firebase.auth.signInWithCredential(credential).await()
                        isPhoneSending = false
                        isOtpVerifying = false
                        onAuthSuccess()
                    } catch (e: Exception) {
                        Log.e("Auth", "Instant phone verification sign-in failed", e)
                        isPhoneSending = false
                        errorMsg = e.localizedMessage ?: "Phone sign-in failed"
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w("Auth", "Phone verification failed", e)
                isPhoneSending = false
                errorMsg = e.localizedMessage ?: "Phone verification failed"
            }

            override fun onCodeSent(
                verId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = verId
                resendToken = token
                isPhoneSending = false
                infoMsg = AppStrings.get(lang, "otpSentNotice")
            }
        }

        val builder = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(cleaned)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
        if (useResendToken && resendToken != null) {
            builder.setForceResendingToken(resendToken!!)
        }
        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    val layoutDir = if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colors.bgTopGlow, colors.bgMain, colors.bgMain)
                    )
                )
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onToggleLang(if (lang == "ar") "en" else "ar") },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.testTag("auth_lang_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (lang == "ar") "English" else "العربية")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = colors.cardBg,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(colors.primary, colors.accent)
                                    )
                                )
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_mnotes_foreground_art),
                                contentDescription = "M Notes Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = AppStrings.get(lang, "signInTitle"),
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = AppStrings.get(lang, "signInSub"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                            textAlign = TextAlign.Center
                        )

                        if (errorMsg != null) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMsg!!,
                                    color = Color(0xFFB91C1C),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        if (infoMsg != null) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.primarySoft,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = infoMsg!!,
                                    color = colors.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // 1. Sign in with Gmail (Google)
                        Button(
                            onClick = {
                                errorMsg = null
                                infoMsg = null
                                isGoogleLoading = true
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isGoogleLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isGoogleLoading = false
                                        errorMsg = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = { detail ->
                                        isGoogleLoading = false
                                        if (!detail.isNullOrBlank()) {
                                            errorMsg = detail
                                        }
                                    }
                                )
                            },
                            enabled = !isGoogleLoading && !isPhoneSending && !isOtpVerifying,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_sign_in_button")
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.get(lang, "signInGoogle"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = colors.borderSoft
                            )
                            Text(
                                text = AppStrings.get(lang, "orDivider"),
                                color = colors.textMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = colors.borderSoft
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // 2. Sign in with Phone Number (SMS OTP)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = colors.bgMain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = AppStrings.get(lang, "phoneLabel"),
                                    color = colors.textMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = phoneInput,
                                            onValueChange = { phoneInput = it },
                                            placeholder = {
                                                Text(
                                                    text = AppStrings.get(lang, "phonePlaceholder"),
                                                    fontSize = 12.5.sp,
                                                    color = colors.textMuted
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.PhoneAndroid,
                                                    contentDescription = null,
                                                    tint = colors.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = colors.primary,
                                                unfocusedBorderColor = colors.borderSoft,
                                                focusedContainerColor = colors.cardBg,
                                                unfocusedContainerColor = colors.cardBg
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("phone_number_input")
                                        )

                                        Button(
                                            onClick = { startPhoneVerification(useResendToken = false) },
                                            enabled = !isPhoneSending && !isGoogleLoading && !isOtpVerifying,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.accent,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .height(52.dp)
                                                .testTag("send_otp_button")
                                        ) {
                                            if (isPhoneSending) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text(
                                                    text = AppStrings.get(lang, "sendOtp"),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = verificationId != null) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            OutlinedTextField(
                                                value = otpCode,
                                                onValueChange = { if (it.length <= 8) otpCode = it },
                                                placeholder = {
                                                    Text(
                                                        text = AppStrings.get(lang, "otpPlaceholder"),
                                                        fontSize = 12.5.sp,
                                                        color = colors.textMuted
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Sms,
                                                        contentDescription = null,
                                                        tint = colors.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = colors.primary,
                                                    unfocusedBorderColor = colors.borderSoft,
                                                    focusedContainerColor = colors.cardBg,
                                                    unfocusedContainerColor = colors.cardBg
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("otp_code_input")
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    val verId = verificationId ?: return@Button
                                                    val code = otpCode.trim()
                                                    if (code.length < 4) {
                                                        errorMsg = AppStrings.get(lang, "otpInvalidError")
                                                        return@Button
                                                    }
                                                    errorMsg = null
                                                    isOtpVerifying = true
                                                    scope.launch {
                                                        try {
                                                            val credential = PhoneAuthProvider.getCredential(verId, code)
                                                            Firebase.auth.signInWithCredential(credential).await()
                                                            isOtpVerifying = false
                                                            onAuthSuccess()
                                                        } catch (e: Exception) {
                                                            Log.w("Auth", "OTP verification failed", e)
                                                            isOtpVerifying = false
                                                            errorMsg = e.localizedMessage ?: AppStrings.get(lang, "otpInvalidError")
                                                        }
                                                    }
                                                },
                                                enabled = !isOtpVerifying && !isPhoneSending,
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.primary,
                                                    contentColor = Color.White
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .testTag("verify_otp_button")
                                            ) {
                                                if (isOtpVerifying) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(18.dp),
                                                        color = Color.White,
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Text(
                                                        text = AppStrings.get(lang, "verifyOtp"),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }

                                            TextButton(
                                                onClick = { startPhoneVerification(useResendToken = true) },
                                                enabled = !isPhoneSending && !isOtpVerifying,
                                                modifier = Modifier.testTag("resend_otp_button")
                                            ) {
                                                Text(
                                                    text = AppStrings.get(lang, "resendOtp"),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.primarySoft,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary),
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textDark
                                    )
                                    Text(
                                        text = AppStrings.get(lang, "ipSub"),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = colors.textMuted
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
