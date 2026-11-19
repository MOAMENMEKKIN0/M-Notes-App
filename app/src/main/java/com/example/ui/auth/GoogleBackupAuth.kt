package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val PREFS_AUTH = "mnotes_passwordless_auth"
private const val KEY_PENDING_EMAIL = "pending_email_link_address"

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
            val msg = e.message.orEmpty()
            if (msg.contains("16") || msg.contains("reauth", ignoreCase = true)) {
                try {
                    val activity = context as? Activity
                        ?: throw IllegalStateException("Activity context required")
                    val fallbackOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(clientId)
                        .setAutoSelectEnabled(false)
                        .build()
                    val fallbackReq = GetCredentialRequest.Builder()
                        .addCredentialOption(fallbackOption)
                        .build()
                    val cred = credentialManager.getCredential(activity, fallbackReq).credential
                    if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                        Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                        onAuthSuccess()
                        return@launch
                    }
                } catch (fallbackErr: Exception) {
                    Log.w("Auth", "Fallback GoogleIdOption failed: ${fallbackErr.message}", fallbackErr)
                    onAuthCancelled(fallbackErr.localizedMessage ?: e.message)
                    return@launch
                }
            }
            onAuthCancelled(e.message)
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun getPendingSignInEmail(context: Context): String {
    return context.applicationContext
        .getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        .getString(KEY_PENDING_EMAIL, "")
        .orEmpty()
}

private fun savePendingSignInEmail(context: Context, email: String) {
    context.applicationContext
        .getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_PENDING_EMAIL, email.trim())
        .apply()
}

private fun clearPendingSignInEmail(context: Context) {
    context.applicationContext
        .getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_PENDING_EMAIL)
        .apply()
}

/**
 * Passwordless Email Verification Link Sign-In (zero password required).
 * Sends a Firebase verification link to the user's email address.
 */
fun sendEmailVerificationSignInLink(
    context: Context,
    rawEmail: String,
    lang: String,
    scope: CoroutineScope,
    onLinkSent: () -> Unit,
    onError: (String) -> Unit
) {
    val email = rawEmail.trim()
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onError(
            if (lang == "en") "Please enter a valid email address."
            else "يرجى إدخال بريد إلكتروني صحيح."
        )
        return
    }
    scope.launch {
        try {
            val authDomain = runCatching {
                Firebase.auth.app.options.projectId?.let { "$it.firebaseapp.com" }
            }.getOrNull() ?: "mnotes.firebaseapp.com"

            val settings = ActionCodeSettings.newBuilder()
                .setUrl("https://$authDomain/finishSignIn")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(context.packageName, true, "26")
                .build()

            Firebase.auth.sendSignInLinkToEmail(email, settings).await()
            savePendingSignInEmail(context, email)
            onLinkSent()
        } catch (e: FirebaseAuthException) {
            Log.w("Auth", "Email verification link error: ${e.errorCode} / ${e.message}", e)
            val msg = if (e.errorCode == "ERROR_OPERATION_NOT_ALLOWED" || e.message?.contains("OPERATION_NOT_ALLOWED", true) == true) {
                if (lang == "en") {
                    "Email Link sign-in is not yet enabled on the backend — use One-Tap Google Email Sign-In above for instant passwordless backup."
                } else {
                    "رابط التحقق عبر البريد غير مفعّل حالياً على السيرفر — استخدم زر الدخول السريع بحساب جوجل بالأعلى للنسخ الاحتياطي الفوري بدون كلمة مرور."
                }
            } else {
                e.localizedMessage ?: "Could not send verification link"
            }
            onError(msg)
        } catch (e: Exception) {
            Log.e("Auth", "Failed to send email verification link", e)
            onError(e.localizedMessage ?: "Could not send verification link")
        }
    }
}

/**
 * Completes Passwordless Email Verification Link Sign-In from either an incoming
 * deep-link Intent or a link pasted by the user.
 */
fun completeEmailVerificationSignInLink(
    context: Context,
    emailLink: String,
    explicitEmail: String = "",
    lang: String = "en",
    scope: CoroutineScope,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val cleanLink = emailLink.trim()
    if (!Firebase.auth.isSignInWithEmailLink(cleanLink)) {
        onError(
            if (lang == "en") "This link is not a valid email verification link."
            else "هذا الرابط غير صالح لتسجيل الدخول."
        )
        return
    }
    val targetEmail = explicitEmail.trim().ifBlank { getPendingSignInEmail(context) }
    if (targetEmail.isBlank()) {
        onError(
            if (lang == "en") "Enter the email address you requested the link for."
            else "أدخل البريد الإلكتروني الذي أُرسل إليه رابط التحقق."
        )
        return
    }
    scope.launch {
        try {
            Firebase.auth.signInWithEmailLink(targetEmail, cleanLink).await()
            clearPendingSignInEmail(context)
            onSuccess(targetEmail)
        } catch (e: Exception) {
            Log.e("Auth", "Failed to verify email sign-in link", e)
            onError(e.localizedMessage ?: "Verification link expired or invalid")
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
