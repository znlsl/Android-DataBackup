package com.xayah.databackup.ui.component

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.xayah.databackup.R
import com.xayah.librootservice.service.RemoteRootService
import com.xayah.librootservice.util.ExceptionUtil
import com.xayah.librootservice.util.withIOContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Creates a [DialogState] and acts as a slot with [DialogState.Insert].
 */
@Composable
fun rememberDialogState(): DialogState {
    val state = remember { DialogState() }
    state.Insert()
    return state
}

class DialogState {
    private var content: (@Composable () -> Unit)? by mutableStateOf(null)

    @Composable
    internal fun Insert() = content?.invoke()

    private fun dismiss() {
        content = null
    }

    /**
     * Return **Pair<Boolean, T>**.
     *
     * If user clicks **confirmButton**, then return **Pair(true, T)**,
     * otherwise return **Pair(false, T)**.
     */
    suspend fun <T> open(
        initialState: T,
        title: String,
        icon: ImageVector? = null,
        confirmText: String? = null,
        dismissText: String? = null,
        onLoading: (suspend () -> Unit)? = null,
        block: @Composable (MutableState<T>) -> Unit,
    ): Pair<Boolean, T> {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { dismiss() }
            content = {
                val uiState = remember { mutableStateOf(initialState) }
                AlertDialog(
                    onDismissRequest = {
                        dismiss()
                        continuation.resume(Pair(false, uiState.value))
                    },
                    confirmButton = {
                        TextButton(text = confirmText ?: stringResource(id = R.string.confirm), onClick = {
                            dismiss()
                            continuation.resume(Pair(true, uiState.value))
                        })
                    },
                    dismissButton = {
                        TextButton(text = dismissText ?: stringResource(id = R.string.cancel), onClick = {
                            dismiss()
                            continuation.resume(Pair(false, uiState.value))
                        })
                    },
                    title = { Text(text = title) },
                    icon = icon?.let { { Icon(imageVector = icon, contentDescription = null) } },
                    text = {
                        if (onLoading == null) block(uiState)
                        else Loader(modifier = Modifier.fillMaxWidth(), onLoading = onLoading, uiState = uiState, content = block)
                    },
                )
            }
        }
    }
}

suspend fun DialogState.openFileOpDialog(context: Context, title: String, filePath: String, icon: ImageVector, text: String) {
    val remoteRootService = RemoteRootService(context)
    var msg: String? = null

    open(
        initialState = false,
        title = title,
        icon = icon,
        onLoading = {
            withIOContext {
                ExceptionUtil.tryService(onFailed = { msg = it }) {
                    remoteRootService.writeText(text, filePath, context)
                }
                remoteRootService.destroyService()
            }
        },
        block = { _ ->
            Text(
                text = if (msg == null) "${context.getString(R.string.succeed)}: $filePath"
                else "${context.getString(R.string.failed)}: $msg\n${context.getString(R.string.remote_service_err_info)}"
            )
        }
    )
}

suspend fun DialogState.openConfirmDialog(context: Context, text: String) = open(
    initialState = false,
    title = context.getString(R.string.prompt),
    icon = Icons.Outlined.Info,
    block = { _ -> Text(text = text) }
)
