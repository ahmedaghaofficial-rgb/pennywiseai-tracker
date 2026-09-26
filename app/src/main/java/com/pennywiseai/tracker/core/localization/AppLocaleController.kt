package com.pennywiseai.tracker.core.localization

import android.content.Context
import android.content.res.Configuration
import com.pennywiseai.tracker.BuildConfig
import java.util.Locale

enum class AppLanguage(val tag: String) {
    ARABIC("ar"),
    ENGLISH("en");

    companion object {
        fun fromTag(tag: String?): AppLanguage = when (tag) {
            ARABIC.tag -> ARABIC
            else -> ENGLISH
        }
    }
}

/**
 * Small, Activity-level locale controller.
 *
 * The upstream app did not have runtime localization. For the Personal build we
 * default to Arabic, while other flavors keep English. The selection is stored
 * in plain SharedPreferences because it must be available synchronously from
 * Activity.attachBaseContext(), before DataStore/Hilt/Compose are ready.
 */
object AppLocaleController {
    private const val PREFS = "app_locale_preferences"
    private const val KEY_LANGUAGE = "language"

    fun defaultLanguage(): AppLanguage =
        if (BuildConfig.IS_PERSONAL_BUILD) AppLanguage.ARABIC else AppLanguage.ENGLISH

    fun getLanguage(context: Context): AppLanguage {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
        return saved?.let(AppLanguage::fromTag) ?: defaultLanguage()
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.tag)
            .apply()
    }

    fun wrap(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}
