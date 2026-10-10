package com.pennywiseai.tracker.core.localization

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalePersistenceTest {
    @Test fun languageSurvivesNewContextAndChangesLayoutDirection() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val original = AppLocaleController.getLanguage(context)
        try {
            AppLocaleController.setLanguage(context, AppLanguage.ARABIC)
            assertEquals(AppLanguage.ARABIC, AppLocaleController.getLanguage(context.applicationContext))
            assertEquals(android.view.View.LAYOUT_DIRECTION_RTL,
                AppLocaleController.wrap(context).resources.configuration.layoutDirection)
            AppLocaleController.setLanguage(context, AppLanguage.ENGLISH)
            assertEquals(AppLanguage.ENGLISH, AppLocaleController.getLanguage(context.applicationContext))
            assertEquals(android.view.View.LAYOUT_DIRECTION_LTR,
                AppLocaleController.wrap(context).resources.configuration.layoutDirection)
        } finally {
            AppLocaleController.setLanguage(context, original)
        }
    }
}
