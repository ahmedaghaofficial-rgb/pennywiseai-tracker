package com.pennywiseai.tracker.core.localization

import com.pennywiseai.tracker.BuildConfig
import com.pennywiseai.tracker.utils.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class FlavorDefaultsTest {
    @Test fun `flavor defaults are isolated`() {
        val expectedLanguage = if (BuildConfig.IS_PERSONAL_BUILD) AppLanguage.ARABIC else AppLanguage.ENGLISH
        val expectedCurrency = if (BuildConfig.IS_PERSONAL_BUILD) "EGP" else "INR"
        assertEquals(expectedLanguage, AppLocaleController.defaultLanguage())
        assertEquals(expectedCurrency, BuildConfig.DEFAULT_CURRENCY)
    }

    @Test fun `default currency is first and all other currencies remain sorted`() {
        assertEquals(
            listOf(BuildConfig.DEFAULT_CURRENCY) + listOf("AED", "EGP", "INR", "USD").filterNot {
                it == BuildConfig.DEFAULT_CURRENCY
            },
            CurrencyUtils.sortCurrencies(listOf("USD", "INR", "EGP", "AED"))
        )
    }

    @Test fun `unknown language falls back to English`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("other"))
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromTag("ar"))
    }
}
