package com.pennywiseai.tracker.data.manager

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Personal build stub: updates are installed manually from our own APK builds. */
@Singleton
class InAppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: StateFlow<Boolean> = _updateAvailable

    private val _updateProgress = MutableStateFlow(0f)
    val updateProgress: StateFlow<Float> = _updateProgress

    private val _updateState = MutableStateFlow(UpdateState.IDLE)
    val updateState: StateFlow<UpdateState> = _updateState

    fun checkForUpdate(
        activity: ComponentActivity,
        snackbarHostState: SnackbarHostState? = null,
        scope: CoroutineScope? = null
    ) = Unit

    fun startUpdate(activity: Activity) = Unit
    fun completeUpdate() = Unit
    fun cleanup() = Unit

    enum class UpdateState {
        IDLE,
        CHECKING,
        AVAILABLE,
        DOWNLOADING,
        DOWNLOADED,
        INSTALLING,
        FAILED
    }
}
