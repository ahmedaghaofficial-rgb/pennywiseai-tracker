package com.pennywiseai.tracker.core.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BalanceAdjustmentLabelsTest {
    @Test fun `stored and older generated descriptions expose the reported balance`() {
        val balance = "₹1,250.00"
        assertEquals(balance, balanceAdjustmentAmount(storedBalanceAdjustmentDescription(balance)))
        assertEquals(balance, balanceAdjustmentAmount("مبلغ غير مسجل لمطابقة رصيد البنك المعلن: $balance"))
        assertNull(balanceAdjustmentAmount("User-written note"))
    }
}
