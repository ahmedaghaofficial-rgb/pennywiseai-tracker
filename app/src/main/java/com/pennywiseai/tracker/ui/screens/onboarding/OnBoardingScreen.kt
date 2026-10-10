package com.pennywiseai.tracker.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import com.pennywiseai.tracker.ui.effects.overScrollVertical
import com.pennywiseai.tracker.data.database.entity.AccountBalanceEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
// TextFieldDefaults already imported above
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pennywiseai.tracker.R
import com.pennywiseai.tracker.core.localization.AppLanguage
import com.pennywiseai.tracker.core.localization.AppLocaleController
import androidx.compose.foundation.isSystemInDarkTheme
import com.pennywiseai.tracker.ui.theme.Dimensions
import com.pennywiseai.tracker.ui.theme.PennyWiseText
import com.pennywiseai.tracker.ui.theme.Spacing
import com.pennywiseai.tracker.ui.theme.income

@Composable
fun OnBoardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnBoardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val smsCaptureAvailable = booleanResource(R.bool.sms_capture_available)

    val stepOrder = remember {
        OnBoardingStep.entries.toList()
    }
    val slideDirection = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
    Scaffold(
        bottomBar = {
            OnBoardingBottomBar(
                uiState = uiState,
                onBack = { viewModel.goToPreviousStep() },
                onNext = {
                    when (uiState.currentStep) {
                        OnBoardingStep.WELCOME -> viewModel.goToNextStep()
                        OnBoardingStep.PROFILE -> viewModel.goToNextStep()
                        OnBoardingStep.PERMISSIONS -> viewModel.goToNextStep()
                        OnBoardingStep.SMS_SCAN -> viewModel.goToNextStep()
                        OnBoardingStep.ACCOUNT_SETUP -> {
                            viewModel.completeOnboarding(onOnboardingComplete)
                        }
                    }
                },
                onSkip = {
                    when (uiState.currentStep) {
                        OnBoardingStep.PERMISSIONS -> {
                            viewModel.skipSmsPermission()
                            viewModel.goToNextStep()
                        }
                        OnBoardingStep.SMS_SCAN -> viewModel.goToNextStep()
                        OnBoardingStep.ACCOUNT_SETUP -> {
                            viewModel.completeOnboarding(onOnboardingComplete)
                        }
                        else -> viewModel.goToNextStep()
                    }
                },
                onStartScan = { viewModel.startSmsScan() },
                smsCaptureAvailable = smsCaptureAvailable
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.currentStep,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            transitionSpec = {
                val targetIndex = stepOrder.indexOf(targetState)
                val initialIndex = stepOrder.indexOf(initialState)
                if (targetIndex > initialIndex) {
                    slideInHorizontally { it * slideDirection } togetherWith slideOutHorizontally { -it * slideDirection }
                } else {
                    slideInHorizontally { -it * slideDirection } togetherWith slideOutHorizontally { it * slideDirection }
                }
            },
            label = "onboarding_step"
        ) { step ->
            when (step) {
                OnBoardingStep.WELCOME -> WelcomeStep()
                OnBoardingStep.PROFILE -> ProfileStep(
                    uiState = uiState,
                    viewModel = viewModel
                )
                OnBoardingStep.PERMISSIONS -> PermissionsStep(
                    uiState = uiState,
                    smsCaptureAvailable = smsCaptureAvailable,
                    onPermissionResult = { viewModel.onSmsPermissionResult(it) },
                    onContinueWithoutSms = {
                        viewModel.skipSmsPermission()
                        viewModel.goToNextStep()
                    }
                )
                OnBoardingStep.SMS_SCAN -> SmsScanStep(uiState = uiState)
                OnBoardingStep.ACCOUNT_SETUP -> AccountSetupStep(
                    uiState = uiState,
                    onSelectAccount = { viewModel.selectAccount(it) }
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = com.pennywiseai.tracker.BuildConfig.APP_DISPLAY_NAME,
            modifier = Modifier
                .size(HERO_ICON_SIZE)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.flosi_welcome_to_app, com.pennywiseai.tracker.BuildConfig.APP_DISPLAY_NAME),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = stringResource(R.string.flosi_your_ai_powered_expense_tracker_that_automatically_det),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        val activity = LocalActivity.current
        val localeContext = androidx.compose.ui.platform.LocalContext.current
        val currentLanguage = AppLocaleController.getLanguage(localeContext)
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    if (currentLanguage != AppLanguage.ARABIC) {
                        AppLocaleController.setLanguage(localeContext, AppLanguage.ARABIC)
                        activity?.recreate()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("العربية")
            }
            OutlinedButton(
                onClick = {
                    if (currentLanguage != AppLanguage.ENGLISH) {
                        AppLocaleController.setLanguage(localeContext, AppLanguage.ENGLISH)
                        activity?.recreate()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("English")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    text = stringResource(R.string.flosi_what_you_ll_set_up),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(R.string.flosi_setup_steps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileStep(
    uiState: OnBoardingUiState,
    viewModel: OnBoardingViewModel
) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectProfileImage(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.flosi_what_should_we_call_you),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        TextField(
            value = uiState.userName,
            onValueChange = { viewModel.updateUserName(it) },
            label = { Text(stringResource(R.string.flosi_your_name)) },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
            )
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.flosi_choose_an_avatar),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(viewModel.avatarDrawables) { index, drawableRes ->
                val isSelected = uiState.profileImageUri == null && uiState.selectedAvatarIndex == index
                Box(
                    modifier = Modifier
                        .size(OPTION_TILE_SIZE)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .then(
                            if (isSelected) Modifier.border(
                                SELECTION_RING_WIDTH,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ) else Modifier
                        )
                        .clickable { viewModel.selectAvatar(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = stringResource(R.string.flosi_avatar_number, index + 1),
                        modifier = Modifier.size(Dimensions.Icon.avatar),
                        colorFilter = ColorFilter.tint(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            item {
                // Gallery picker option
                Box(
                    modifier = Modifier
                        .size(OPTION_TILE_SIZE)
                        .clip(CircleShape)
                        .background(
                            if (uiState.profileImageUri != null) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .then(
                            if (uiState.profileImageUri != null) Modifier.border(
                                SELECTION_RING_WIDTH,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ) else Modifier
                        )
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.profileImageUri != null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.flosi_photo_selected),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimensions.Icon.large)
                        )
                    } else {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.flosi_pick_a_background_color),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            viewModel.backgroundColors.forEachIndexed { index, colorInt ->
                val isSelected = uiState.selectedBackgroundColor == index
                Box(
                    modifier = Modifier
                        .size(Dimensions.Icon.avatar)
                        .clip(CircleShape)
                        .background(Color(colorInt))
                        .then(
                            if (isSelected) Modifier.border(
                                SELECTION_RING_WIDTH,
                                MaterialTheme.colorScheme.onSurface,
                                CircleShape
                            ) else Modifier
                        )
                        .clickable { viewModel.selectBackgroundColor(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.flosi_selected),
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(Dimensions.Icon.medium)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionsStep(
    uiState: OnBoardingUiState,
    smsCaptureAvailable: Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onContinueWithoutSms: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readSmsGranted = permissions[Manifest.permission.READ_SMS] == true
        onPermissionResult(readSmsGranted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MailOutline,
            contentDescription = null,
            modifier = Modifier.size(HERO_ICON_SIZE_SMALL),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (!smsCaptureAvailable) {
            Text(
                text = stringResource(R.string.flosi_automatic_detection_is_off_in_this_test_build),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = stringResource(R.string.flosi_this_sideload_test_intentionally_disables_sms_and_bank),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.flosi_automatic_transaction_capture_is_not_being_removed_fro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(Spacing.md)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Button(
                onClick = onContinueWithoutSms,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.flosi_continue_without_sms))
            }

            return@Column
        }

        Text(
            text = stringResource(R.string.flosi_enable_automatic_detection),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = stringResource(R.string.flosi_sms_detection_description, com.pennywiseai.tracker.BuildConfig.APP_DISPLAY_NAME),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    text = stringResource(R.string.flosi_your_privacy_matters),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = listOf(
                        stringResource(R.string.flosi_privacy_transactions_only),
                        stringResource(R.string.flosi_privacy_on_device),
                        stringResource(R.string.flosi_privacy_no_personal_messages),
                        stringResource(R.string.flosi_privacy_revoke_access)
                    ).joinToString("\n") { "• $it" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        if (uiState.smsPermissionGranted) {
            val incomeColor = MaterialTheme.colorScheme.income
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = incomeColor.copy(alpha = if (isSystemInDarkTheme()) 0.15f else 0.12f)
                ),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = incomeColor
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = stringResource(R.string.flosi_permissions_granted_tap_continue_to_proceed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = incomeColor
                    )
                }
            }
        } else {
            Button(
                onClick = {
                    val permissions = mutableListOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.flosi_enable_permissions))
            }
        }
    }
}

@Composable
private fun SmsScanStep(uiState: OnBoardingUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.Icon.emptyStateContainer)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.flosi_scanning_your_messages),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            if (uiState.scanTotal > 0) {
                val progress = uiState.scanProcessed.toFloat() / uiState.scanTotal.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(R.string.flosi_messages_processed_count, uiState.scanProcessed, uiState.scanTotal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.scanParsed > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.flosi_onboarding_transactions_found, uiState.scanParsed, uiState.scanParsed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (uiState.scanEstimatedRemaining > 0) {
                    val seconds = uiState.scanEstimatedRemaining / 1000
                    Text(
                        text = pluralStringResource(R.plurals.flosi_onboarding_seconds_remaining, seconds.toInt(), seconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(R.string.flosi_preparing_scan),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (uiState.scanCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.Icon.emptyStateContainer),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.flosi_scan_complete),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            if (uiState.scanSaved > 0) {
                Text(
                    text = pluralStringResource(
                        R.plurals.flosi_onboarding_scan_saved,
                        uiState.scanSaved,
                        uiState.scanSaved,
                        pluralStringResource(R.plurals.flosi_onboarding_messages_count, uiState.scanTotal, uiState.scanTotal)
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stringResource(R.string.flosi_no_transactions_found_you_can_add_them_manually_later),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Not started yet
            Icon(
                imageVector = Icons.Filled.MailOutline,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.Icon.emptyStateContainer),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.flosi_scan_your_messages),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = stringResource(R.string.flosi_we_ll_scan_your_sms_messages_to_find_bank_transactions),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccountSetupStep(
    uiState: OnBoardingUiState,
    onSelectAccount: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        if (uiState.accounts.isEmpty()) {
            Icon(
                imageVector = Icons.Filled.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.Icon.emptyStateContainer),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.flosi_you_re_all_set),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = stringResource(R.string.flosi_no_accounts_were_detected_yet_you_can_set_up_your_main),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = stringResource(R.string.flosi_select_your_main_account),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = stringResource(R.string.flosi_choose_the_account_you_use_most_often_this_will_be_sho),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            uiState.accounts.forEach { account ->
                val accountKey = "${account.bankName}_${account.accountLast4}"
                val isSelected = uiState.selectedAccountKey == accountKey

                // The muted "on" colour has to follow the container. Selecting a
                // row swaps its fill to primaryContainer, and onSurfaceVariant —
                // a colour paired with `surface` — then sat on it at almost no
                // contrast, making the masked digits vanish on the selected row.
                val mutedColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(
                        alpha = Dimensions.Alpha.subtitle
                    )
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    onClick = { onSelectAccount(accountKey) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = if (isSelected) BorderStroke(
                        Spacing.xxs,
                        MaterialTheme.colorScheme.primary
                    ) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md)
                            .defaultMinSize(minHeight = Dimensions.Component.minTouchTarget),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                        ) {
                            Text(
                                text = account.bankName,
                                style = MaterialTheme.typography.titleSmall
                            )
                            // Mobile-money wallets have no account number.
                            if (account.accountLast4 != AccountBalanceEntity.WALLET_ACCOUNT_MARKER) {
                                Text(
                                    text = "****${account.accountLast4}",
                                    style = PennyWiseText.amountSmall,
                                    color = mutedColor
                                )
                            }
                        }
                        Text(
                            text = "${account.currency} ${account.balance}",
                            style = PennyWiseText.amountRow
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.flosi_selected),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(Dimensions.Icon.inline)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: OnBoardingStep,
    modifier: Modifier = Modifier
) {
    val steps = OnBoardingStep.entries
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isActive = step == currentStep
            val isPast = index < steps.indexOf(currentStep)
            Box(
                modifier = Modifier
                    .padding(horizontal = Spacing.xs)
                    .size(if (isActive) STEP_DOT_ACTIVE else STEP_DOT_INACTIVE)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        }
                    )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnBoardingBottomBar(
    uiState: OnBoardingUiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onStartScan: () -> Unit,
    smsCaptureAvailable: Boolean
) {
    val isFirstStep = uiState.currentStep == OnBoardingStep.WELCOME
    val canGoBack = !isFirstStep && !uiState.isScanning

    // Keep the controls above the system navigation bar under edge-to-edge (#563):
    // the Scaffold's bottomBar slot doesn't apply navigation-bar insets on its own,
    // so without this the gesture/3-button nav bar overlaps the Next button.
    Column(modifier = Modifier.navigationBarsPadding()) {
        StepIndicator(
            currentStep = uiState.currentStep,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.sm)
        )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Back button
        if (canGoBack) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.flosi_back)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Skip / CTA button area
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            when (uiState.currentStep) {
                OnBoardingStep.WELCOME -> {
                    Button(onClick = onNext) {
                        Text(stringResource(R.string.flosi_get_started))
                    }
                }

                OnBoardingStep.PROFILE -> {
                    Button(
                        onClick = onNext,
                        enabled = uiState.userName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.flosi_save_continue))
                    }
                }

                OnBoardingStep.PERMISSIONS -> {
                    if (!smsCaptureAvailable) {
                        Button(onClick = onNext) {
                            Text(stringResource(R.string.flosi_continue))
                        }
                    } else {
                        if (!uiState.smsPermissionGranted) {
                            TextButton(onClick = onSkip) {
                                Text(stringResource(R.string.flosi_skip))
                            }
                        }
                        if (uiState.smsPermissionGranted) {
                            Button(onClick = onNext) {
                                Text(stringResource(R.string.flosi_continue))
                            }
                        }
                    }
                }

                OnBoardingStep.SMS_SCAN -> {
                    if (!uiState.isScanning && !uiState.scanCompleted) {
                        TextButton(onClick = onSkip) {
                            Text(stringResource(R.string.flosi_skip))
                        }
                        Button(onClick = onStartScan) {
                            Text(stringResource(R.string.flosi_start_scanning))
                        }
                    } else if (uiState.isScanning) {
                        TextButton(onClick = onSkip) {
                            Text(stringResource(R.string.flosi_skip))
                        }
                    } else if (uiState.scanCompleted) {
                        Button(onClick = onNext) {
                            Text(stringResource(R.string.flosi_continue))
                        }
                    }
                }

                OnBoardingStep.ACCOUNT_SETUP -> {
                    if (uiState.accounts.isEmpty()) {
                        Button(onClick = onNext) {
                            Text(stringResource(R.string.flosi_finish))
                        }
                    } else {
                        if (uiState.selectedAccountKey == null) {
                            TextButton(onClick = onSkip) {
                                Text(stringResource(R.string.flosi_skip))
                            }
                        }
                        Button(
                            onClick = onNext,
                            enabled = uiState.selectedAccountKey != null || uiState.accounts.isEmpty()
                        ) {
                            if (uiState.isCompleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(Dimensions.Icon.medium),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(stringResource(R.string.flosi_finish))
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

// Onboarding-only geometry. These are intentionally larger than any in-app
// token: full-screen illustrative moments, not list chrome.
private val HERO_ICON_SIZE = 100.dp
private val HERO_ICON_SIZE_SMALL = 80.dp
private val OPTION_TILE_SIZE = 72.dp
private val SELECTION_RING_WIDTH = 3.dp
private val STEP_DOT_ACTIVE = Spacing.sm
private val STEP_DOT_INACTIVE = 6.dp
