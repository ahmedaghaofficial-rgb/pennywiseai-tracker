package com.pennywiseai.tracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pennywiseai.tracker.R
import com.pennywiseai.tracker.domain.security.BiometricCapability

@Composable
fun localizedBiometricErrorMessage(capability: BiometricCapability): String = stringResource(when (capability) {
    BiometricCapability.Available -> R.string.flosi_biometric_available
    BiometricCapability.NoHardware -> R.string.flosi_biometric_no_hardware
    BiometricCapability.HardwareUnavailable -> R.string.flosi_biometric_hardware_unavailable
    BiometricCapability.NoneEnrolled -> R.string.flosi_biometric_none_enrolled
    BiometricCapability.SecurityUpdateRequired -> R.string.flosi_biometric_security_update
    BiometricCapability.Unsupported -> R.string.flosi_biometric_unsupported
    BiometricCapability.Unknown -> R.string.flosi_biometric_unknown
})
