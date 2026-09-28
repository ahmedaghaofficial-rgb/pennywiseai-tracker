package com.pennywiseai.tracker.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import com.pennywiseai.tracker.ui.effects.overScrollVertical
import com.pennywiseai.tracker.data.database.entity.AccountBalanceEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.pennywiseai.tracker.R
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pennywiseai.tracker.core.Constants
import com.pennywiseai.tracker.core.localization.AppLanguage
import com.pennywiseai.tracker.core.localization.AppLocaleController
import com.pennywiseai.tracker.ui.components.CustomTitleTopAppBar
import com.pennywiseai.tracker.ui.components.SupportDevelopmentDialog
import com.pennywiseai.tracker.ui.components.cards.GroupedColumn
import com.pennywiseai.tracker.ui.components.cards.GroupedList
import com.pennywiseai.tracker.ui.components.cards.GroupedRow
import com.pennywiseai.tracker.ui.components.cards.IconTile
import com.pennywiseai.tracker.ui.components.cards.ListItemPosition
import com.pennywiseai.tracker.ui.components.cards.RowLabels
import com.pennywiseai.tracker.ui.components.cards.SectionHeaderV2
import com.pennywiseai.tracker.ui.theme.Dimensions
import com.pennywiseai.tracker.ui.theme.Spacing
import com.pennywiseai.tracker.ui.theme.amber_light
import com.pennywiseai.tracker.ui.theme.amber_dark
import com.pennywiseai.tracker.ui.theme.orange_light
import com.pennywiseai.tracker.ui.theme.orange_dark
import com.pennywiseai.tracker.ui.theme.green_light
import com.pennywiseai.tracker.ui.theme.green_dark
import com.pennywiseai.tracker.ui.theme.teal_light
import com.pennywiseai.tracker.ui.theme.teal_dark
import com.pennywiseai.tracker.ui.theme.blue_light
import com.pennywiseai.tracker.ui.theme.blue_dark
import com.pennywiseai.tracker.ui.theme.indigo_light
import com.pennywiseai.tracker.ui.theme.indigo_dark
import com.pennywiseai.tracker.ui.theme.red_light
import com.pennywiseai.tracker.ui.theme.red_dark
import com.pennywiseai.tracker.ui.theme.pink_light
import com.pennywiseai.tracker.ui.theme.pink_dark
import com.pennywiseai.tracker.ui.theme.purple_light
import com.pennywiseai.tracker.ui.theme.purple_dark
import com.pennywiseai.tracker.ui.theme.cyan_light
import com.pennywiseai.tracker.ui.theme.cyan_dark
import com.pennywiseai.tracker.ui.theme.yellow_light
import com.pennywiseai.tracker.ui.theme.yellow_dark
import com.pennywiseai.tracker.ui.theme.grey_light
import com.pennywiseai.tracker.ui.theme.grey_dark
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.pennywiseai.tracker.ui.viewmodel.ThemeViewModel
import com.pennywiseai.tracker.data.preferences.NumberFormatStyle
import com.pennywiseai.tracker.utils.CurrencyFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToUnrecognizedSms: () -> Unit = {},
    onNavigateToManageAccounts: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToRules: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    onNavigateToLoans: () -> Unit = {},
    onNavigateToRecurring: () -> Unit = {},
    onNavigateToTransactionGroups: () -> Unit = {},
    onNavigateToExchangeRates: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToImportStatement: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    appLockViewModel: com.pennywiseai.tracker.ui.viewmodel.AppLockViewModel = hiltViewModel(),
    permissionViewModel: com.pennywiseai.tracker.ui.viewmodel.PermissionViewModel = hiltViewModel()
) {
    val themeUiState by themeViewModel.themeUiState.collectAsStateWithLifecycle()
    val appLockUiState by appLockViewModel.uiState.collectAsStateWithLifecycle()
    val downloadState by settingsViewModel.downloadState.collectAsStateWithLifecycle()
    val downloadProgress by settingsViewModel.downloadProgress.collectAsStateWithLifecycle()
    val downloadedMB by settingsViewModel.downloadedMB.collectAsStateWithLifecycle()
    val totalMB by settingsViewModel.totalMB.collectAsStateWithLifecycle()
    val isDeveloperModeEnabled by settingsViewModel.isDeveloperModeEnabled.collectAsStateWithLifecycle(initialValue = false)
    val smsScanMonths by settingsViewModel.smsScanMonths.collectAsStateWithLifecycle(initialValue = 3)
    val smsScanAllTime by settingsViewModel.smsScanAllTime.collectAsStateWithLifecycle(initialValue = false)
    val smsScanUseCustomDate by settingsViewModel.smsScanUseCustomDate.collectAsStateWithLifecycle(initialValue = false)
    val smsScanCustomDate by settingsViewModel.smsScanCustomDate.collectAsStateWithLifecycle(initialValue = null)
    val baseCurrency by settingsViewModel.baseCurrency.collectAsStateWithLifecycle(initialValue = "")
    val numberFormatStyle by settingsViewModel.numberFormatStyle.collectAsStateWithLifecycle(initialValue = NumberFormatStyle.AUTO)
    val budgetCycleStartDay by settingsViewModel.budgetCycleStartDay.collectAsStateWithLifecycle(initialValue = 1)
    val importExportMessage by settingsViewModel.importExportMessage.collectAsStateWithLifecycle()
    val exportedBackupFile by settingsViewModel.exportedBackupFile.collectAsStateWithLifecycle()
    val deleteAllTransactionsCount by settingsViewModel.deleteAllTransactionsCount.collectAsStateWithLifecycle()
    val isDeletingAllTransactions by settingsViewModel.isDeletingAllTransactions.collectAsStateWithLifecycle()
    val deleteAllTransactionsResult by settingsViewModel.deleteAllTransactionsResult.collectAsStateWithLifecycle()
    val unifiedCurrencyMode by settingsViewModel.unifiedCurrencyMode.collectAsStateWithLifecycle(initialValue = false)
    val countCreditCardAsExpense by settingsViewModel.countCreditCardAsExpense.collectAsStateWithLifecycle(initialValue = false)
    val displayCurrency by settingsViewModel.displayCurrency.collectAsStateWithLifecycle(initialValue = "")
    val availableCurrencies by settingsViewModel.availableCurrencies.collectAsStateWithLifecycle()
    val accounts by settingsViewModel.accounts.collectAsStateWithLifecycle()
    val mainAccountKey by settingsViewModel.mainAccountKey.collectAsStateWithLifecycle()
    val useContactsForVpa by settingsViewModel.useContactsForVpa.collectAsStateWithLifecycle(initialValue = false)
    val isProEntitled by settingsViewModel.isProEntitled.collectAsStateWithLifecycle()
    val scheduledFolderBackupEnabled by settingsViewModel.scheduledFolderBackupEnabled.collectAsStateWithLifecycle(initialValue = false)
    val scheduledFolderBackupLastTimestamp by settingsViewModel.scheduledFolderBackupLastTimestamp.collectAsStateWithLifecycle(initialValue = null)
    val requestFolderPicker by settingsViewModel.requestFolderPicker.collectAsStateWithLifecycle()
    var showUpgradeSheet by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    // F-Droid builds have no Play billing, so they show a "Support development"
    // tip jar instead of the (un-buyable) Pro upsell. Play builds keep Pro.
    val isFdroidBuild = com.pennywiseai.tracker.BuildConfig.IS_FDROID_BUILD
    // Launches the runtime permission request. If granted, we flip the
    // preference on; if denied, leave the switch off so the user can try
    // again without us silently turning the feature on later.
    val readContactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) settingsViewModel.setUseContactsForVpa(true)
    }
    var showSmsScanDialog by remember { mutableStateOf(false) }
    var showSmsScanDatePicker by remember { mutableStateOf(false) }
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showDisplayCurrencyDialog by remember { mutableStateOf(false) }
    var showNumberFormatDialog by remember { mutableStateOf(false) }
    var showBudgetCycleDialog by remember { mutableStateOf(false) }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    var showMainAccountDropdown by remember { mutableStateOf(false) }
    val permissionUiState by permissionViewModel.uiState.collectAsStateWithLifecycle()
    val hasNotificationAccess = permissionUiState.hasNotificationAccess
    val context = LocalContext.current
    val activity = androidx.activity.compose.LocalActivity.current
    val currentLanguage = AppLocaleController.getLanguage(context)
    var showLanguageDialog by remember { mutableStateOf(false) }
    val notificationAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permissionViewModel.refreshNotificationAccess()
    }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                settingsViewModel.importBackup(it)
            }
        }
    )

    // File picker for CSV transaction import
    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                settingsViewModel.importCsv(it)
            }
        }
    )

    // File saver for export
    val exportSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            uri?.let {
                settingsViewModel.saveBackupToFile(it)
            }
        }
    )

    val backupFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let { settingsViewModel.onBackupFolderSelected(it) }
        }
    )

    LaunchedEffect(requestFolderPicker) {
        if (requestFolderPicker) {
            backupFolderLauncher.launch(null)
            settingsViewModel.onFolderPickerLaunched()
        }
    }

    // Scroll behaviors for collapsible TopAppBar
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollBehaviorLarge = scrollBehaviorSmall
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehaviorLarge,
                title = stringResource(R.string.flosi_settings),
                hasBackButton = true,
                hasActionButton = true,
                navigationContent = { SettingsNavigationContent(onNavigateBack) },
                hazeState = hazeState
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .background(MaterialTheme.colorScheme.background)
                .overScrollVertical()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(Dimensions.Padding.content),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Commercial/support UI belongs to the upstream distribution.
            // The personal flavor has all existing gated features unlocked and
            // intentionally shows no purchase, license, or tip-jar surface.
            if (!com.pennywiseai.tracker.BuildConfig.IS_PERSONAL_BUILD) {
                if (isFdroidBuild) {
                    SectionHeaderV2(title = stringResource(R.string.support_title))
                    SettingsGroup {
                        SettingsNavItem(
                            icon = Icons.Default.Favorite,
                            iconBgColor = yellow_light,
                            iconTint = yellow_dark,
                            title = stringResource(R.string.support_title),
                            subtitle = stringResource(R.string.support_subtitle),
                            onClick = { showSupportDialog = true },
                            position = ListItemPosition.Single,
                        )
                    }
                } else {
                    SectionHeaderV2(title = "PennyWise Pro")
                    SettingsGroup {
                        SettingsNavItem(
                            icon = Icons.Default.AutoAwesome,
                            iconBgColor = yellow_light,
                            iconTint = yellow_dark,
                            title = if (isProEntitled) "PennyWise Pro" else "Upgrade to PennyWise Pro",
                            subtitle = if (isProEntitled) {
                                "Active · all power features unlocked"
                            } else {
                                "Unlimited rules, statements, exports, and more"
                            },
                            onClick = { showUpgradeSheet = true },
                            position = ListItemPosition.Single,
                        )
                    }
                }
            }

            // ── Personalization ──
            SectionHeaderV2(title = stringResource(R.string.flosi_personalization))
            SettingsGroup {
                SettingsNavItem(
                    icon = Icons.Default.Palette,
                    iconBgColor = orange_light,
                    iconTint = orange_dark,
                    title = stringResource(R.string.flosi_appearance),
                    subtitle = stringResource(R.string.flosi_theme_colors_fonts_navigation),
                    onClick = onNavigateToAppearance,
                    position = ListItemPosition.Top
                )
                SettingsNavItem(
                    icon = Icons.Default.Language,
                    iconBgColor = blue_light,
                    iconTint = blue_dark,
                    title = stringResource(R.string.flosi_language),
                    subtitle = stringResource(R.string.flosi_choose_arabic_or_english),
                    trailingText = if (currentLanguage == AppLanguage.ARABIC) "العربية" else "English",
                    onClick = { showLanguageDialog = true },
                    position = ListItemPosition.Bottom
                )
            }

            // ── Currency ──
            SectionHeaderV2(title = stringResource(R.string.flosi_currency))
            SettingsGroup {
                SettingsSwitchRow(
                    icon = Icons.Default.CurrencyExchange,
                    iconBgColor = green_light,
                    iconTint = green_dark,
                    title = stringResource(R.string.flosi_unified_currency_mode),
                    subtitle = stringResource(R.string.flosi_convert_all_transactions_to_display_currency),
                    checked = unifiedCurrencyMode,
                    onCheckedChange = { settingsViewModel.setUnifiedCurrencyMode(it) },
                    position = ListItemPosition.Top
                )
                AnimatedVisibility(visible = unifiedCurrencyMode) {
                    SettingsNavItem(
                        icon = Icons.Default.AttachMoney,
                        iconBgColor = teal_light,
                        iconTint = teal_dark,
                        title = stringResource(R.string.flosi_display_currency),
                        subtitle = stringResource(R.string.flosi_all_amounts_shown_in_this_currency),
                        onClick = { showDisplayCurrencyDialog = true },
                        position = ListItemPosition.Middle,
                        trailingText = "${CurrencyFormatter.getCurrencySymbol(displayCurrency)} $displayCurrency"
                    )
                }
                SettingsNavItem(
                    icon = Icons.Default.SwapHoriz,
                    iconBgColor = blue_light,
                    iconTint = blue_dark,
                    title = stringResource(R.string.flosi_exchange_rates),
                    subtitle = stringResource(R.string.flosi_view_and_customize_rates),
                    onClick = onNavigateToExchangeRates,
                    position = ListItemPosition.Middle
                )
                SettingsSwitchRow(
                    icon = Icons.Default.CreditCard,
                    iconBgColor = indigo_light,
                    iconTint = indigo_dark,
                    title = stringResource(R.string.flosi_count_card_spend_as_expense),
                    subtitle = "Include credit-card spend in \"Spent this month\"",
                    checked = countCreditCardAsExpense,
                    onCheckedChange = { settingsViewModel.setCountCreditCardAsExpense(it) },
                    position = ListItemPosition.Middle
                )
                SettingsDropdownItem(
                    icon = Icons.Default.Flag,
                    iconBgColor = indigo_light,
                    iconTint = indigo_dark,
                    title = stringResource(R.string.flosi_default_currency),
                    subtitle = stringResource(R.string.flosi_currency_used_for_conversions),
                    currentValue = "${CurrencyFormatter.getCurrencySymbol(baseCurrency)} $baseCurrency",
                    expanded = showCurrencyDropdown,
                    onExpandedChange = { showCurrencyDropdown = it },
                    position = ListItemPosition.Middle
                ) {
                    availableCurrencies.forEach { currency ->
                        DropdownMenuItem(
                            text = {
                                Text("${CurrencyFormatter.getCurrencySymbol(currency)} $currency")
                            },
                            onClick = {
                                settingsViewModel.updateBaseCurrency(currency)
                                showCurrencyDropdown = false
                            },
                            leadingIcon = if (currency == baseCurrency) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else null
                        )
                    }
                }

                // Main account → sets the default currency (unless explicitly chosen above).
                if (accounts.isNotEmpty()) {
                    val mainAccount = accounts.firstOrNull {
                        "${it.bankName}_${it.accountLast4}" == mainAccountKey
                    }
                    SettingsDropdownItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBgColor = purple_light,
                        iconTint = purple_dark,
                        title = stringResource(R.string.flosi_main_account),
                        subtitle = stringResource(R.string.flosi_sets_your_default_currency),
                        currentValue = mainAccount?.let { acc ->
                            val name = acc.alias?.takeIf { it.isNotBlank() } ?: acc.bankName
                            AccountBalanceEntity.accountLabel(name, acc.accountLast4)
                        } ?: "Not set",
                        expanded = showMainAccountDropdown,
                        onExpandedChange = { showMainAccountDropdown = it },
                        position = ListItemPosition.Middle
                    ) {
                        accounts.forEach { account ->
                            val name = account.alias?.takeIf { it.isNotBlank() } ?: account.bankName
                            val label = AccountBalanceEntity.accountLabel(name, account.accountLast4)
                            val key = "${account.bankName}_${account.accountLast4}"
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    settingsViewModel.setMainAccount(account)
                                    showMainAccountDropdown = false
                                },
                                leadingIcon = if (key == mainAccountKey) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }

                SettingsNavItem(
                    icon = Icons.Default.Numbers,
                    iconBgColor = green_light,
                    iconTint = green_dark,
                    title = stringResource(R.string.flosi_number_format),
                    subtitle = stringResource(R.string.flosi_how_large_amounts_are_grouped),
                    onClick = { showNumberFormatDialog = true },
                    position = ListItemPosition.Bottom,
                    trailingText = numberFormatStyleLabel(numberFormatStyle)
                )
            }

            // ── Budget ──
            // The cycle start day is a budgeting concept, but it changes how
            // Home / Analytics bucket transactions, so it lives up here next
            // to the other "display" knobs rather than buried in Data
            // Management with the budgets list.
            SectionHeaderV2(title = stringResource(R.string.flosi_budget))
            SettingsGroup {
                SettingsNavItem(
                    icon = Icons.Default.DateRange,
                    iconBgColor = teal_light,
                    iconTint = teal_dark,
                    title = stringResource(R.string.flosi_budget_cycle_start_day),
                    subtitle = "Shifts the start of each monthly budget period; e.g. 25 means your cycle runs 25th → 24th",
                    onClick = { showBudgetCycleDialog = true },
                    position = ListItemPosition.Single,
                    trailingText = ordinalSuffix(budgetCycleStartDay)
                )
            }

            // ── Contacts ──
            SectionHeaderV2(title = stringResource(R.string.flosi_contacts))
            SettingsGroup {
                SettingsSwitchRow(
                    icon = Icons.Default.Contacts,
                    iconBgColor = teal_light,
                    iconTint = teal_dark,
                    title = stringResource(R.string.flosi_replace_upi_vpas_with_contact_names),
                    subtitle = "Show 'John Doe' instead of '9876543210@paytm'. Needs contacts permission.",
                    checked = useContactsForVpa,
                    onCheckedChange = { wantsOn ->
                        if (wantsOn) {
                            val alreadyGranted = androidx.core.content.ContextCompat
                                .checkSelfPermission(
                                    context,
                                    android.Manifest.permission.READ_CONTACTS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (alreadyGranted) {
                                settingsViewModel.setUseContactsForVpa(true)
                            } else {
                                readContactsLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                            }
                        } else {
                            settingsViewModel.setUseContactsForVpa(false)
                        }
                    },
                    position = ListItemPosition.Single
                )
            }

            // ── Security ──
            SectionHeaderV2(title = stringResource(R.string.flosi_security))
            SettingsGroup {
                SettingsSwitchRow(
                    icon = Icons.Default.Lock,
                    iconBgColor = red_light,
                    iconTint = red_dark,
                    title = stringResource(R.string.flosi_app_lock),
                    subtitle = if (appLockUiState.canUseBiometric) {
                        "Protect your data with biometric authentication"
                    } else {
                        appLockUiState.biometricCapability.getErrorMessage()
                    },
                    checked = appLockUiState.isLockEnabled,
                    onCheckedChange = { appLockViewModel.setAppLockEnabled(it) },
                    enabled = appLockUiState.canUseBiometric,
                    position = if (appLockUiState.isLockEnabled) ListItemPosition.Top else ListItemPosition.Single
                )
                AnimatedVisibility(visible = appLockUiState.isLockEnabled) {
                    SettingsNavItem(
                        icon = Icons.Default.Timer,
                        iconBgColor = pink_light,
                        iconTint = pink_dark,
                        title = stringResource(R.string.flosi_lock_timeout),
                        subtitle = when (appLockUiState.timeoutMinutes) {
                            0 -> "Lock immediately"
                            1 -> "After 1 minute"
                            else -> "After ${appLockUiState.timeoutMinutes} minutes"
                        },
                        onClick = { showTimeoutDialog = true },
                        position = ListItemPosition.Bottom
                    )
                }
            }

            // ── Data Management ──
            SectionHeaderV2(title = stringResource(R.string.flosi_data_management))
            SettingsGroup {
                SettingsNavItem(
                    icon = Icons.Default.AccountBalance,
                    iconBgColor = red_light,
                    iconTint = red_dark,
                    title = stringResource(R.string.flosi_manage_accounts),
                    subtitle = stringResource(R.string.flosi_view_and_manage_your_bank_accounts),
                    onClick = onNavigateToManageAccounts,
                    position = ListItemPosition.Top
                )
                SettingsNavItem(
                    icon = Icons.Default.Category,
                    iconBgColor = purple_light,
                    iconTint = purple_dark,
                    title = stringResource(R.string.flosi_categories),
                    subtitle = stringResource(R.string.flosi_manage_expense_and_income_categories),
                    onClick = onNavigateToCategories,
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.AutoAwesome,
                    iconBgColor = orange_light,
                    iconTint = orange_dark,
                    title = stringResource(R.string.flosi_smart_rules),
                    subtitle = stringResource(R.string.flosi_automatic_transaction_categorization),
                    onClick = onNavigateToRules,
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconBgColor = green_light,
                    iconTint = green_dark,
                    title = stringResource(R.string.flosi_budgets),
                    subtitle = stringResource(R.string.flosi_track_spending_limits_by_category),
                    onClick = onNavigateToBudgets,
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.SwapHoriz,
                    iconBgColor = amber_light,
                    iconTint = amber_dark,
                    title = stringResource(R.string.flosi_loans),
                    subtitle = stringResource(R.string.flosi_track_money_lent_and_borrowed),
                    onClick = onNavigateToLoans,
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.EventRepeat,
                    iconBgColor = green_light,
                    iconTint = green_dark,
                    title = stringResource(R.string.flosi_recurring),
                    subtitle = stringResource(R.string.flosi_auto_add_scheduled_cash_manual_transactions),
                    onClick = onNavigateToRecurring,
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.Folder,
                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    title = stringResource(R.string.flosi_transaction_groups),
                    subtitle = stringResource(R.string.flosi_organise_transactions_under_a_topic),
                    onClick = onNavigateToTransactionGroups,
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.Upload,
                    iconBgColor = blue_light,
                    iconTint = blue_dark,
                    title = stringResource(R.string.flosi_export_data),
                    subtitle = stringResource(R.string.flosi_backup_all_data_to_a_file),
                    onClick = { settingsViewModel.exportBackup() },
                    position = ListItemPosition.Middle
                )
                SettingsSwitchRow(
                    icon = Icons.Default.Backup,
                    iconBgColor = purple_light,
                    iconTint = purple_dark,
                    title = stringResource(R.string.flosi_automatic_folder_backup),
                    subtitle = if (scheduledFolderBackupEnabled) {
                        "Daily backup at 2:00 AM to your chosen folder"
                    } else if (!isProEntitled) {
                        "Pro · Save a backup to a folder every day at 2:00 AM"
                    } else {
                        "Save a backup to a folder every day at 2:00 AM"
                    },
                    checked = scheduledFolderBackupEnabled,
                    // Scheduling daily backups is a Pro feature. Turning it ON while
                    // free routes to the paywall; turning it OFF is always allowed so
                    // a lapsed/downgraded user can still stop scheduled backups.
                    onCheckedChange = { enabled ->
                        if (enabled && !isProEntitled) {
                            showUpgradeSheet = true
                        } else {
                            settingsViewModel.setScheduledFolderBackupEnabled(enabled)
                        }
                    },
                    position = ListItemPosition.Middle
                )
                if (scheduledFolderBackupEnabled) {
                    SettingsNavItem(
                        icon = Icons.Default.SaveAlt,
                        iconBgColor = green_light,
                        iconTint = green_dark,
                        title = stringResource(R.string.flosi_back_up_now),
                        subtitle = scheduledFolderBackupLastTimestamp?.let { timestamp ->
                            val formatted = java.time.Instant.ofEpochMilli(timestamp)
                                .atZone(java.time.ZoneId.systemDefault())
                                .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))
                            "Last backup: $formatted"
                        } ?: "Run a backup to your folder now",
                        onClick = { settingsViewModel.backupToFolderNow() },
                        position = ListItemPosition.Middle
                    )
                    SettingsNavItem(
                        icon = Icons.Default.FolderOpen,
                        iconBgColor = amber_light,
                        iconTint = amber_dark,
                        title = stringResource(R.string.flosi_change_backup_folder),
                        subtitle = stringResource(R.string.flosi_pick_a_different_folder_for_automatic_backups),
                        onClick = { settingsViewModel.requestChangeBackupFolder() },
                        position = ListItemPosition.Middle
                    )
                }
                SettingsNavItem(
                    icon = Icons.Default.Download,
                    iconBgColor = cyan_light,
                    iconTint = cyan_dark,
                    title = stringResource(R.string.flosi_import_data),
                    subtitle = stringResource(R.string.flosi_restore_data_from_backup),
                    onClick = { importLauncher.launch("*/*") },
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.Download,
                    iconBgColor = cyan_light,
                    iconTint = cyan_dark,
                    title = stringResource(R.string.flosi_import_transactions_csv),
                    subtitle = stringResource(R.string.flosi_import_from_a_pennywise_csv_export),
                    onClick = { csvImportLauncher.launch("*/*") },
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.Description,
                    iconBgColor = indigo_light,
                    iconTint = indigo_dark,
                    title = stringResource(R.string.flosi_import_statement),
                    subtitle = stringResource(R.string.flosi_import_from_gpay_phonepe_paytm),
                    onClick = onNavigateToImportStatement,
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.Sms,
                    iconBgColor = orange_light,
                    iconTint = orange_dark,
                    title = stringResource(R.string.flosi_unrecognized_sms),
                    subtitle = stringResource(R.string.flosi_view_and_report_unsupported_bank_messages),
                    onClick = onNavigateToUnrecognizedSms,
                    position = ListItemPosition.Middle
                )
                SettingsNavItem(
                    icon = Icons.Default.CalendarMonth,
                    iconBgColor = teal_light,
                    iconTint = teal_dark,
                    title = stringResource(R.string.flosi_sms_scan_period),
                    subtitle = when {
                        smsScanAllTime -> "Scan all SMS messages"
                        smsScanUseCustomDate -> {
                            val formattedDate = smsScanCustomDate?.let { formatSmsScanCustomDate(it) }
                            if (formattedDate != null) {
                                "Scan from $formattedDate to today"
                            } else {
                                "Scan from a custom start date to today"
                            }
                        }
                        else -> "Scan last $smsScanMonths months"
                    },
                    onClick = { showSmsScanDialog = true },
                    position = ListItemPosition.Middle,
                    trailingText = when {
                        smsScanAllTime -> "All Time"
                        smsScanUseCustomDate -> smsScanCustomDate?.let { formatSmsScanCustomDateShort(it) } ?: "Custom"
                        else -> "$smsScanMonths mo"
                    }
                )
                SettingsNavItem(
                    icon = Icons.Default.DeleteForever,
                    iconBgColor = red_light,
                    iconTint = red_dark,
                    title = stringResource(R.string.flosi_delete_all_transactions),
                    subtitle = stringResource(R.string.flosi_clear_your_transaction_history_accounts_and_budgets_stay),
                    onClick = { settingsViewModel.requestDeleteAllTransactions() },
                    position = ListItemPosition.Bottom
                )
            }

            // ── Notifications ──
            SectionHeaderV2(title = stringResource(R.string.flosi_notifications))
            SettingsGroup {
                SettingsNavItem(
                    icon = Icons.Default.Notifications,
                    iconBgColor = indigo_light,
                    iconTint = indigo_dark,
                    title = stringResource(R.string.flosi_bank_notification_access),
                    subtitle = if (hasNotificationAccess) "Enabled" else "Tap to enable bank app notifications",
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        notificationAccessLauncher.launch(intent)
                    },
                    position = ListItemPosition.Single,
                    trailingText = if (hasNotificationAccess) "On" else "Off"
                )
            }

            // ── AI Features ──
            SectionHeaderV2(title = stringResource(R.string.flosi_ai_features))
            SettingsGroup {
                AiChatSettingsItem(
                    downloadState = downloadState,
                    downloadProgress = downloadProgress,
                    downloadedMB = downloadedMB,
                    totalMB = totalMB,
                    onDownload = { settingsViewModel.startModelDownload() },
                    onCancel = { settingsViewModel.cancelDownload() },
                    onDelete = { settingsViewModel.deleteModel() }
                )
            }

            // ── Developer ──
            SectionHeaderV2(title = stringResource(R.string.flosi_developer))
            SettingsGroup {
                SettingsSwitchRow(
                    icon = Icons.Default.Code,
                    iconBgColor = grey_light,
                    iconTint = grey_dark,
                    title = stringResource(R.string.flosi_developer_mode),
                    subtitle = stringResource(R.string.flosi_show_technical_information_in_chat),
                    checked = isDeveloperModeEnabled,
                    onCheckedChange = { settingsViewModel.toggleDeveloperMode(it) },
                    position = ListItemPosition.Single
                )
            }

            // ── Support & Community ──
            SectionHeaderV2(title = stringResource(R.string.flosi_support_community))
            SettingsGroup {
                SettingsNavItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    iconBgColor = pink_light,
                    iconTint = pink_dark,
                    title = stringResource(R.string.flosi_help_faq),
                    subtitle = stringResource(R.string.flosi_frequently_asked_questions_and_help),
                    onClick = onNavigateToFaq,
                    position = ListItemPosition.Top
                )
                SettingsNavItem(
                    icon = Icons.Default.BugReport,
                    iconBgColor = blue_light,
                    iconTint = blue_dark,
                    title = stringResource(R.string.flosi_report_an_issue),
                    subtitle = stringResource(R.string.flosi_submit_bug_reports_on_github),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sarim2000/pennywiseai-tracker/issues/new/choose"))
                        context.startActivity(intent)
                    },
                    position = ListItemPosition.Bottom,
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew
                )
            }

            // App Version
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "PennyWise v${com.pennywiseai.tracker.BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }

    // ── Dialogs ──

    // App Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.flosi_language)) },
            text = {
                Column {
                    listOf(
                        AppLanguage.ARABIC to "العربية",
                        AppLanguage.ENGLISH to "English"
                    ).forEach { (language, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = language == currentLanguage,
                                    onClick = {
                                        AppLocaleController.setLanguage(context, language)
                                        showLanguageDialog = false
                                        activity?.recreate()
                                    }
                                )
                                .padding(vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == currentLanguage,
                                onClick = {
                                    AppLocaleController.setLanguage(context, language)
                                    showLanguageDialog = false
                                    activity?.recreate()
                                }
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.flosi_close))
                }
            }
        )
    }

    // Display Currency Dialog
    if (showDisplayCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showDisplayCurrencyDialog = false },
            title = { Text(stringResource(R.string.flosi_display_currency)) },
            text = {
                // Scrollable: the full currency list overflows the dialog's max
                // height, so without this the entries below the fold (e.g. MXN)
                // are unreachable. (#615)
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    availableCurrencies.forEach { currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currency == displayCurrency,
                                    onClick = {
                                        settingsViewModel.setDisplayCurrency(currency)
                                        showDisplayCurrencyDialog = false
                                    }
                                )
                                .padding(vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currency == displayCurrency,
                                onClick = {
                                    settingsViewModel.setDisplayCurrency(currency)
                                    showDisplayCurrencyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = "${CurrencyFormatter.getCurrencySymbol(currency)} $currency",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDisplayCurrencyDialog = false }) {
                    Text(stringResource(R.string.flosi_cancel))
                }
            }
        )
    }

    // Number Format Dialog
    if (showNumberFormatDialog) {
        AlertDialog(
            onDismissRequest = { showNumberFormatDialog = false },
            title = { Text(stringResource(R.string.flosi_number_format)) },
            text = {
                Column {
                    NumberFormatStyle.entries.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = style == numberFormatStyle,
                                    onClick = {
                                        settingsViewModel.updateNumberFormatStyle(style)
                                        showNumberFormatDialog = false
                                    }
                                )
                                .padding(vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = style == numberFormatStyle,
                                onClick = {
                                    settingsViewModel.updateNumberFormatStyle(style)
                                    showNumberFormatDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Column {
                                Text(
                                    text = numberFormatStyleLabel(style),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = numberFormatStyleExample(style),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNumberFormatDialog = false }) {
                    Text(stringResource(R.string.flosi_cancel))
                }
            }
        )
    }

    // Budget Cycle Start Day Dialog
    if (showBudgetCycleDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetCycleDialog = false },
            title = { Text(stringResource(R.string.flosi_budget_cycle_start_day)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Pick the day each monthly budget cycle starts. e.g. 25 means the cycle runs from the 25th through the 24th of the next month.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))

                    (1..31).forEach { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = day == budgetCycleStartDay,
                                    onClick = {
                                        settingsViewModel.updateBudgetCycleStartDay(day)
                                        showBudgetCycleDialog = false
                                    }
                                )
                                .padding(vertical = Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = day == budgetCycleStartDay,
                                onClick = {
                                    settingsViewModel.updateBudgetCycleStartDay(day)
                                    showBudgetCycleDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = ordinalSuffix(day),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBudgetCycleDialog = false }) {
                    Text(stringResource(R.string.flosi_cancel))
                }
            }
        )
    }

    // SMS Scan Period Dialog
    if (showSmsScanDialog) {
        AlertDialog(
            onDismissRequest = { showSmsScanDialog = false },
            title = { Text(stringResource(R.string.flosi_sms_scan_period)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = stringResource(R.string.flosi_choose_how_far_back_to_scan_sms_messages_for_transactions),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))

                    val options = listOf(-1, -2) + listOf(1, 2, 3, 6, 12, 24)
                    options.forEach { months ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (months) {
                                        -1 -> {
                                            settingsViewModel.updateSmsScanAllTime(true)
                                            showSmsScanDialog = false
                                        }
                                        -2 -> {
                                            showSmsScanDialog = false
                                            showSmsScanDatePicker = true
                                        }
                                        else -> {
                                            settingsViewModel.updateSmsScanMonths(months)
                                            settingsViewModel.updateSmsScanAllTime(false)
                                            showSmsScanDialog = false
                                        }
                                    }
                                }
                                .padding(vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isSelected = when (months) {
                                -1 -> smsScanAllTime
                                -2 -> smsScanUseCustomDate && !smsScanAllTime
                                else -> smsScanMonths == months && !smsScanAllTime && !smsScanUseCustomDate
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    when (months) {
                                        -1 -> {
                                            settingsViewModel.updateSmsScanAllTime(true)
                                            showSmsScanDialog = false
                                        }
                                        -2 -> {
                                            showSmsScanDialog = false
                                            showSmsScanDatePicker = true
                                        }
                                        else -> {
                                            settingsViewModel.updateSmsScanMonths(months)
                                            settingsViewModel.updateSmsScanAllTime(false)
                                            showSmsScanDialog = false
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Text(
                                text = when (months) {
                                    -1 -> "All Time"
                                    -2 -> {
                                        val formattedDate = smsScanCustomDate?.let { formatSmsScanCustomDate(it) }
                                        if (formattedDate != null) "Custom date ($formattedDate)" else "Custom date"
                                    }
                                    1 -> "1 month"
                                    24 -> "2 years"
                                    else -> "$months months"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSmsScanDialog = false }) {
                    Text(stringResource(R.string.flosi_cancel))
                }
            }
        )
    }

    if (showSmsScanDatePicker) {
        val todayMillis = java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val initialSelectedDateMillis = smsScanCustomDate
            ?: java.time.LocalDate.now()
                .minusMonths(3)
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= todayMillis
                }
            }
        )

        // Cancelling/dismissing the picker returns to the scan-period dialog rather than
        // silently dropping the user back to Settings (they came here to change the period).
        fun reopenScanDialog() {
            showSmsScanDatePicker = false
            showSmsScanDialog = true
        }

        DatePickerDialog(
            onDismissRequest = { reopenScanDialog() },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            settingsViewModel.updateSmsScanCustomDate(millis)
                        }
                        showSmsScanDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.flosi_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { reopenScanDialog() }) {
                    Text(stringResource(R.string.flosi_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete-all-transactions confirmation. Irreversible and unbatched, so it
    // asks for the word DELETE rather than a single tap, names the exact number
    // of rows, and points at Export Data first.
    deleteAllTransactionsCount?.let { count ->
        var confirmationText by rememberSaveable(count) { mutableStateOf("") }
        val confirmed = confirmationText.trim().equals("DELETE", ignoreCase = false)

        AlertDialog(
            onDismissRequest = {
                if (!isDeletingAllTransactions) settingsViewModel.cancelDeleteAllTransactions()
            },
            icon = {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.flosi_delete_all_transactions)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(
                        if (count == 1) {
                            "This permanently deletes your 1 transaction, along with its splits and tags. It cannot be undone."
                        } else {
                            "This permanently deletes all $count transactions, along with their splits and tags. It cannot be undone."
                        }
                    )
                    Text(
                        "Your accounts, budgets, loans, categories and rules are kept. " +
                            "Export Data first if you might want this history back.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = confirmationText,
                        onValueChange = { confirmationText = it },
                        singleLine = true,
                        enabled = !isDeletingAllTransactions,
                        label = { Text(stringResource(R.string.flosi_type_delete_to_confirm)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { settingsViewModel.deleteAllTransactions(count) },
                    enabled = confirmed && !isDeletingAllTransactions,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (isDeletingAllTransactions) "Deleting…" else "Delete All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { settingsViewModel.cancelDeleteAllTransactions() },
                    enabled = !isDeletingAllTransactions
                ) {
                    Text(stringResource(R.string.flosi_cancel))
                }
            }
        )
    }

    deleteAllTransactionsResult?.let { message ->
        AlertDialog(
            onDismissRequest = { settingsViewModel.clearDeleteAllTransactionsResult() },
            title = { Text(stringResource(R.string.flosi_transactions)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { settingsViewModel.clearDeleteAllTransactionsResult() }) {
                    Text(stringResource(R.string.flosi_ok))
                }
            }
        )
    }

    // Show import/export message
    importExportMessage?.let { message ->
        if (exportedBackupFile != null && message.contains("successfully! Choose")) {
            showExportOptionsDialog = true
        } else {
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(5000)
                settingsViewModel.clearImportExportMessage()
            }

            AlertDialog(
                onDismissRequest = { settingsViewModel.clearImportExportMessage() },
                title = { Text(stringResource(R.string.flosi_backup_status)) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { settingsViewModel.clearImportExportMessage() }) {
                        Text(stringResource(R.string.flosi_ok))
                    }
                }
            )
        }
    }

    // Export options dialog
    if (showExportOptionsDialog && exportedBackupFile != null) {
        val timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss")
        )
        val fileName = "PennyWise_Backup_$timestamp.pennywisebackup"

        AlertDialog(
            onDismissRequest = {
                showExportOptionsDialog = false
                settingsViewModel.clearImportExportMessage()
            },
            title = { Text(stringResource(R.string.flosi_save_backup)) },
            text = {
                Column {
                    Text(stringResource(R.string.flosi_backup_created_successfully))
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(stringResource(R.string.flosi_choose_how_you_want_to_save_it), style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            exportSaveLauncher.launch(fileName)
                            showExportOptionsDialog = false
                            settingsViewModel.clearImportExportMessage()
                        }
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.flosi_save_to_files))
                    }

                    TextButton(
                        onClick = {
                            settingsViewModel.shareBackup()
                            showExportOptionsDialog = false
                            settingsViewModel.clearImportExportMessage()
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.flosi_share))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportOptionsDialog = false
                        settingsViewModel.clearImportExportMessage()
                    }
                ) {
                    Text(stringResource(R.string.flosi_cancel))
                }
            }
        )
    }

    // Lock Timeout Dialog
    if (showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text(stringResource(R.string.flosi_lock_timeout)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = stringResource(R.string.flosi_choose_when_to_lock_the_app_after_it_goes_to_background),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))

                    val timeoutOptions = listOf(
                        0 to "Immediately",
                        1 to "1 minute",
                        5 to "5 minutes",
                        15 to "15 minutes"
                    )

                    timeoutOptions.forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    appLockViewModel.setTimeoutMinutes(minutes)
                                    showTimeoutDialog = false
                                }
                                .padding(vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = appLockUiState.timeoutMinutes == minutes,
                                onClick = {
                                    appLockViewModel.setTimeoutMinutes(minutes)
                                    showTimeoutDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text(stringResource(R.string.flosi_done))
                }
            }
        )
    }

    if (showUpgradeSheet) {
        com.pennywiseai.tracker.presentation.paywall.UpgradeSheet(
            onDismiss = { showUpgradeSheet = false },
        )
    }

    if (showSupportDialog) {
        SupportDevelopmentDialog(onDismiss = { showSupportDialog = false })
    }
}

// ── Reusable Settings Components ──
//
// Row chrome — tonal surface, grouped-corner shape, padding, minimum height,
// the tinted icon circle, title/subtitle typography — lives in the shared
// `GroupedList` / `GroupedRow` / `IconTile` / `RowLabels` primitives, so a
// settings row and a grouped row on any other screen are literally the same
// object. These wrappers only add the settings-specific trailing affordance.

@Composable
private fun SettingsGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    GroupedList(content = content)
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    position: ListItemPosition,
    trailingText: String? = null,
    trailingIcon: ImageVector = Icons.Default.ChevronRight
) {
    GroupedRow(position = position, onClick = onClick) {
        IconTile(icon = icon, containerColor = iconBgColor, contentColor = iconTint)
        RowLabels(title = title, subtitle = subtitle)
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // The chevron is a hint, not a control — at 20dp it stops competing
        // with the leading icon for attention the way a 24dp one did.
        Icon(
            trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimensions.Icon.inline).then(
                if (trailingIcon == Icons.Default.ChevronRight &&
                    LocalLayoutDirection.current == LayoutDirection.Rtl
                ) Modifier.graphicsLayer(scaleX = -1f) else Modifier
            )
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    position: ListItemPosition,
    enabled: Boolean = true
) {
    GroupedRow(
        position = position,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) }
    ) {
        IconTile(icon = icon, containerColor = iconBgColor, contentColor = iconTint)
        RowLabels(
            title = title,
            subtitle = subtitle,
            subtitleColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.error
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdownItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    currentValue: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    position: ListItemPosition,
    dropdownContent: @Composable ColumnScope.() -> Unit
) {
    GroupedColumn(position = position) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(icon = icon, containerColor = iconBgColor, contentColor = iconTint)
            RowLabels(title = title, subtitle = subtitle)
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            TextField(
                value = currentValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.flosi_currency)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = MaterialTheme.shapes.large,
                colors = TextFieldDefaults.colors(
                    // A field nested inside an already-tonal row needs a step
                    // of contrast against it, otherwise the input boundary
                    // disappears.
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                content = dropdownContent
            )
        }
    }
}

@Composable
private fun AiChatSettingsItem(
    downloadState: DownloadState,
    downloadProgress: Int,
    downloadedMB: Long,
    totalMB: Long,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    GroupedColumn(
        position = ListItemPosition.Single,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(
                icon = Icons.Default.AutoAwesome,
                containerColor = yellow_light,
                contentColor = yellow_dark
            )
            RowLabels(
                title = stringResource(R.string.flosi_ai_chat_assistant),
                subtitle = when (downloadState) {
                    DownloadState.NOT_DOWNLOADED -> "Download AI model (${Constants.ModelDownload.MODEL_SIZE_MB} MB)"
                    DownloadState.DOWNLOADING -> "Downloading AI model..."
                    DownloadState.PAUSED -> "Download interrupted"
                    DownloadState.COMPLETED -> "AI model ready for chat"
                    DownloadState.FAILED -> "Download failed"
                    DownloadState.ERROR_INSUFFICIENT_SPACE -> "Not enough storage space"
                }
            )

            when (downloadState) {
                DownloadState.NOT_DOWNLOADED -> {
                    Button(onClick = onDownload) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.flosi_download))
                    }
                }
                DownloadState.DOWNLOADING -> {
                    Text(
                        text = "$downloadProgress%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                DownloadState.PAUSED -> {
                    Button(onClick = onDownload) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.flosi_retry))
                    }
                }
                DownloadState.COMPLETED -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.flosi_downloaded),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimensions.Icon.medium)
                        )
                        TextButton(onClick = onDelete) {
                            Text(stringResource(R.string.flosi_delete))
                        }
                    }
                }
                DownloadState.FAILED -> {
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.flosi_retry))
                    }
                }
                DownloadState.ERROR_INSUFFICIENT_SPACE -> {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = stringResource(R.string.flosi_error),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimensions.Icon.medium)
                    )
                }
            }
        }

        // Progress details during download
        AnimatedVisibility(
            visible = downloadState == DownloadState.DOWNLOADING,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                LinearProgressIndicator(
                    progress = { downloadProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "$downloadedMB MB / $totalMB MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(stringResource(R.string.flosi_cancel_download))
                }
            }
        }

        // Info about AI features
        if (downloadState == DownloadState.NOT_DOWNLOADED ||
            downloadState == DownloadState.ERROR_INSUFFICIENT_SPACE
        ) {
            HorizontalDivider()
            Text(
                text = "Chat with AI about your expenses and get financial insights. " +
                        "All conversations stay private on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsNavigationContent(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier
            .animateContentSize()
            .padding(start = Spacing.md)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onNavigateBack,
            ),
    ) {
        IconButton(
            onClick = onNavigateBack,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.flosi_back),
                modifier = Modifier.size(Dimensions.Icon.inline)
            )
        }
    }
}

private fun numberFormatStyleLabel(style: NumberFormatStyle): String = when (style) {
    NumberFormatStyle.AUTO -> "Auto"
    NumberFormatStyle.INDIAN -> "Indian"
    NumberFormatStyle.INTERNATIONAL -> "International"
}

private fun numberFormatStyleExample(style: NumberFormatStyle): String = when (style) {
    NumberFormatStyle.AUTO -> "Matches each currency (₹1,50,000 · $150,000)"
    NumberFormatStyle.INDIAN -> "1,50,000 (lakh / crore)"
    NumberFormatStyle.INTERNATIONAL -> "150,000 (thousand / million)"
}

/**
 * English ordinal suffix for the budget cycle start day — "1st", "2nd", "3rd",
 * "4th"… "11th", "12th", "13th" follow the standard rule that the last two
 * digits decide the suffix (the 11/12/13 teens are always "th").
 */
private fun ordinalSuffix(day: Int): String {
    val safe = day.coerceIn(1, 31)
    val suffix = when {
        safe in 11..13 -> "th"
        safe % 10 == 1 -> "st"
        safe % 10 == 2 -> "nd"
        safe % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$safe$suffix"
}

private fun formatSmsScanCustomDate(dateMillis: Long): String {
    return java.time.Instant.ofEpochMilli(dateMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
}

private fun formatSmsScanCustomDateShort(dateMillis: Long): String {
    return java.time.Instant.ofEpochMilli(dateMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
}
