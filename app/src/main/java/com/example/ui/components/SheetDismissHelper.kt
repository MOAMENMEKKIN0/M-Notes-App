package com.example.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * Coordinates safe teardown of Material 3 ModalBottomSheet dialogs so focus, IME,
 * and system navigation-bar insets finish cleanly before the dialog window's
 * ViewRootImpl is detached (preventing Android ViewRootImpl showInsets(2, false)
 * errors on detached windows).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class SafeSheetDismisser(
    private val sheetState: SheetState,
    private val scope: CoroutineScope,
    private val clearFocusAndKeyboard: () -> Unit,
    private val onDismissRef: () -> () -> Unit
) {
    private var isDismissing by mutableStateOf(false)

    /** Use for ModalBottomSheet's `onDismissRequest` (already animated by swipe/scrim/back). */
    fun onSystemDismiss() {
        if (isDismissing) return
        isDismissing = true
        clearFocusAndKeyboard()
        onDismissRef().invoke()
    }

    /** Use for programmatic button clicks (Close, Cancel, Switch Sheet where action does not close current sheet). */
    fun dismiss(afterDismiss: (() -> Unit)? = null) {
        if (isDismissing) return
        isDismissing = true
        clearFocusAndKeyboard()
        scope.launch {
            runCatching {
                if (sheetState.isVisible) {
                    sheetState.hide()
                }
            }
            yield()
            onDismissRef().invoke()
            afterDismiss?.invoke()
        }
    }

    /** Use for primary actions (Save, Apply, Pick) that close the sheet themselves without invoking cancel/exit onDismiss. */
    fun dismissWith(action: () -> Unit) {
        if (isDismissing) return
        isDismissing = true
        clearFocusAndKeyboard()
        scope.launch {
            runCatching {
                if (sheetState.isVisible) {
                    sheetState.hide()
                }
            }
            yield()
            action()
        }
    }

    /** Use for suspending primary actions (e.g. cropping/saving) that close the sheet upon completion. */
    fun dismissWithSuspend(action: suspend () -> Unit, onFinally: () -> Unit = {}) {
        if (isDismissing) return
        isDismissing = true
        clearFocusAndKeyboard()
        scope.launch {
            try {
                runCatching {
                    if (sheetState.isVisible) {
                        sheetState.hide()
                    }
                }
                yield()
                action()
            } finally {
                onFinally()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSafeSheetDismisser(
    sheetState: SheetState,
    onDismiss: () -> Unit
): SafeSheetDismisser {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val latestOnDismiss by rememberUpdatedState(onDismiss)

    return remember(sheetState, scope, focusManager, keyboardController) {
        SafeSheetDismisser(
            sheetState = sheetState,
            scope = scope,
            clearFocusAndKeyboard = {
                runCatching { focusManager.clearFocus(force = true) }
                runCatching { keyboardController?.hide() }
            },
            onDismissRef = { latestOnDismiss }
        )
    }
}
