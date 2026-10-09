package com.pennywiseai.tracker.ui.viewmodel

import androidx.biometric.BiometricPrompt
import com.pennywiseai.tracker.R
import org.junit.Assert.assertEquals
import org.junit.Test

class BiometricAuthErrorResourceTest {
    @Test fun `terminal errors offer an actionable localized resource`() {
        assertEquals(R.string.flosi_biometric_lockout_temporary, biometricAuthErrorResource(BiometricPrompt.ERROR_LOCKOUT))
        assertEquals(R.string.flosi_biometric_lockout_permanent, biometricAuthErrorResource(BiometricPrompt.ERROR_LOCKOUT_PERMANENT))
        assertEquals(R.string.flosi_biometric_device_credential_missing, biometricAuthErrorResource(BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL))
        assertEquals(R.string.flosi_auth_failed, biometricAuthErrorResource(-1))
    }
}
