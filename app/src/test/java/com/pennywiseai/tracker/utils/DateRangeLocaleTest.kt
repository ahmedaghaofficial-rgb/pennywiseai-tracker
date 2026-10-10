package com.pennywiseai.tracker.utils

import com.pennywiseai.tracker.presentation.common.TimePeriod
import com.pennywiseai.tracker.presentation.common.chipLabel
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DateRangeLocaleTest {
    @Test fun `custom and budget cycle labels follow the requested locale immediately`() {
        val start = LocalDate.of(2026, 1, 11)
        val end = LocalDate.of(2026, 2, 10)
        val english = Locale.ENGLISH
        val arabic = Locale.forLanguageTag("ar")

        assertNotEquals(
            DateRangeUtils.formatDateRange(start to end, locale = english),
            DateRangeUtils.formatDateRange(start to end, locale = arabic)
        )
        assertNotEquals(
            TimePeriod.THIS_MONTH.chipLabel(11, null, today = end, locale = english),
            TimePeriod.THIS_MONTH.chipLabel(11, null, today = end, locale = arabic)
        )
    }
}
