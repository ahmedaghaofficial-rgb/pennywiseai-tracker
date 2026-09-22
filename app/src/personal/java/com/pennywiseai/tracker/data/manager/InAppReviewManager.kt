package com.pennywiseai.tracker.data.manager

import android.content.Context
import androidx.activity.ComponentActivity
import com.pennywiseai.tracker.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Personal build stub: no Play Store review prompts. */
@Singleton
class InAppReviewManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun checkAndShowReviewIfEligible(
        activity: ComponentActivity,
        transactionCount: Int = 0
    ) = Unit

    suspend fun checkAfterSmsImport(
        activity: ComponentActivity,
        importedCount: Int
    ) = Unit
}
