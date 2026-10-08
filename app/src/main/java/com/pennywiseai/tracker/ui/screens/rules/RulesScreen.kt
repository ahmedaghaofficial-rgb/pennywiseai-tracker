package com.pennywiseai.tracker.ui.screens.rules

import com.pennywiseai.tracker.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.pennywiseai.tracker.ui.effects.overScrollVertical
import com.pennywiseai.tracker.ui.effects.rememberOverscrollFlingBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pennywiseai.tracker.data.rules.RuleSharingCodec
import com.pennywiseai.tracker.domain.usecase.BatchApplyResult
import com.pennywiseai.tracker.domain.usecase.DryRunResult
import com.pennywiseai.tracker.ui.components.CustomTitleTopAppBar
import com.pennywiseai.tracker.ui.components.cards.PennyWiseCardV2
import com.pennywiseai.tracker.ui.components.cards.SectionHeaderV2
import com.pennywiseai.tracker.ui.icons.localizedCategoryName
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.pennywiseai.tracker.ui.theme.Dimensions
import com.pennywiseai.tracker.ui.theme.Spacing
import com.pennywiseai.tracker.ui.viewmodel.RulesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateRule: () -> Unit,
    onNavigateToEditRule: (String) -> Unit,
    onNavigateToDuplicateRule: (String) -> Unit,
    viewModel: RulesViewModel = hiltViewModel()
) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val batchApplyProgress by viewModel.batchApplyProgress.collectAsStateWithLifecycle()
    val batchApplyResult by viewModel.batchApplyResult.collectAsStateWithLifecycle()
    val dryRunResult by viewModel.dryRunResult.collectAsStateWithLifecycle()
    val canCreateMoreRules by viewModel.canCreateMoreRules.collectAsStateWithLifecycle()
    val sharingMessage by viewModel.sharingMessage.collectAsStateWithLifecycle()
    var showUpgradeSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Rule sharing (#741). CreateDocument/OpenDocument keep the file in the
    // user's own storage — nothing leaves the device unless they share it.
    val exportRulesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(RuleSharingCodec.MIME_TYPE)
    ) { uri -> uri?.let { viewModel.exportRules(it) } }
    val importRulesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importRules(it) } }

    var showBatchApplyDialog by remember { mutableStateOf(false) }
    var selectedRuleForBatch by remember { mutableStateOf<com.pennywiseai.tracker.domain.model.rule.TransactionRule?>(null) }

    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollBehaviorLarge = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hazeState = remember { HazeState() }

    // Reset dialog state needs to be outside the lambda
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviorLarge.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehaviorLarge,
                title = stringResource(R.string.flosi_smart_rules),
                hasBackButton = true,
                hasActionButton = true,
                navigationContent = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.flosi_full_navigate_back)
                        )
                    }
                },
                actionContent = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.flosi_more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.flosi_full_export_rules)) },
                                leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    if (RuleSharingCodec.exportable(rules).isEmpty()) {
                                        viewModel.reportNothingToExport()
                                    } else {
                                        exportRulesLauncher.launch(
                                            "pennywise-rules.${RuleSharingCodec.FILE_EXTENSION}"
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.flosi_full_import_rules)) },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    // Some file pickers don't offer JSON files under the
                                    // strict MIME type, so accept anything and let the
                                    // decoder reject what isn't a rule set.
                                    importRulesLauncher.launch(arrayOf("*/*"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.flosi_full_reset_to_defaults)) },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    showResetDialog = true
                                }
                            )
                        }
                    }
                },
                hazeState = hazeState
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (canCreateMoreRules) onNavigateToCreateRule()
                    else showUpgradeSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.flosi_full_create_rule))
            }
        }
    ) { paddingValues ->

        sharingMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.clearSharingMessage() },
                title = { Text(stringResource(R.string.flosi_smart_rules)) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearSharingMessage() }) {
                        Text(stringResource(R.string.flosi_ok))
                    }
                }
            )
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringResource(R.string.flosi_full_reset_rules)) },
                text = { Text(stringResource(R.string.flosi_full_reset_all_rules_to_default_settings_your_custom_settings_will_be)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetToDefaults()
                            showResetDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.flosi_full_reset))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(R.string.flosi_cancel))
                    }
                }
            )
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .background(MaterialTheme.colorScheme.background)
                    .overScrollVertical(),
                contentPadding = PaddingValues(
                    start = Dimensions.Padding.content,
                    end = Dimensions.Padding.content,
                    top = Dimensions.Padding.content + paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                ),
                state = lazyListState,
                flingBehavior = rememberOverscrollFlingBehavior { lazyListState },
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Info Card
                item {
                    // Use a primaryContainer background so the onPrimaryContainer icon/text
                    // are legible. On the default surface card they're near-invisible in
                    // dark mode (low-contrast primary tone on a dark surface).
                    PennyWiseCardV2(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.flosi_full_automatic_categorization),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.flosi_full_enable_rules_to_automatically_categorize_your_transactions_based),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Group rules by category for better organization
                item {
                    val groupedRules = rules.groupBy(::ruleGroup)

                    groupedRules.forEach { (category, categoryRules) ->
                        if (categoryRules.isNotEmpty()) {
                            SectionHeaderV2(title = stringResource(when (category) { RuleGroup.DAILY -> R.string.flosi_rule_group_daily; RuleGroup.INCOME -> R.string.flosi_rule_group_income; RuleGroup.RECURRING -> R.string.flosi_rule_group_recurring; RuleGroup.BANKING -> R.string.flosi_rule_group_banking; RuleGroup.HEALTH -> R.string.flosi_rule_group_health; RuleGroup.OTHER -> R.string.flosi_rule_group_other }))

                            categoryRules.forEach { rule ->
                                RuleCard(
                                    rule = rule,
                                    onToggle = { isActive ->
                                        viewModel.toggleRule(rule.id, isActive)
                                    },
                                    onEdit = {
                                        onNavigateToEditRule(rule.id)
                                    },
                                    onDuplicate = {
                                        onNavigateToDuplicateRule(rule.id)
                                    },
                                    onDelete = {
                                        viewModel.deleteRule(rule.id)
                                    },
                                    onApplyToPast = {
                                        selectedRuleForBatch = rule
                                        showBatchApplyDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Help text at the bottom
                item {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text(
                        text = stringResource(R.string.flosi_full_rules_are_applied_automatically_to_new_transactions_higher_priori),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    )
                }
            }
        }
    }

    // Batch Apply Dialog
    if (showBatchApplyDialog && selectedRuleForBatch != null) {
        BatchApplyDialog(
            rule = selectedRuleForBatch!!,
            progress = batchApplyProgress,
            result = batchApplyResult,
            dryRunResult = dryRunResult,
            isLoading = isLoading,
            onDismiss = {
                showBatchApplyDialog = false
                selectedRuleForBatch = null
                viewModel.clearBatchApplyResult()
            },
            onPreview = {
                viewModel.previewRule(selectedRuleForBatch!!)
            },
            onApplyToAll = {
                viewModel.applyRuleToPastTransactions(selectedRuleForBatch!!, applyToUncategorizedOnly = false)
            },
            onApplyToUncategorized = {
                viewModel.applyRuleToPastTransactions(selectedRuleForBatch!!, applyToUncategorizedOnly = true)
            }
        )
    }

    if (showUpgradeSheet) {
        com.pennywiseai.tracker.presentation.paywall.UpgradeSheet(
            onDismiss = { showUpgradeSheet = false },
        )
    }
}

@Composable
private fun displayRuleName(rule: com.pennywiseai.tracker.domain.model.rule.TransactionRule): String =
    if (rule.isSystemTemplate && rule.name == "Small Payments to Food")
        stringResource(R.string.flosi_rule_template_name)
    else rule.name

@Composable
private fun RuleCard(
    rule: com.pennywiseai.tracker.domain.model.rule.TransactionRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onApplyToPast: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    PennyWiseCardV2(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = displayRuleName(rule),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                rule.description?.let { description ->
                    Text(
                        text = if (rule.isSystemTemplate && description == "Categorize small expense payments (under 200) as Food & Dining") stringResource(R.string.flosi_rule_template_desc) else description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Show simple condition summary
                val conditionSummary = when {
                    rule.name.contains("Small Payments", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_small)
                    rule.name.contains("UPI Cashback", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_cashback)
                    rule.name.contains("Salary", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_salary)
                    rule.name.contains("Rent", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_rent)
                    rule.name.contains("EMI", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_emi)
                    rule.name.contains("Investment", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_investment)
                    rule.name.contains("Subscription", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_subscription)
                    rule.name.contains("Fuel", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_fuel)
                    rule.name.contains("Healthcare", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_health)
                    rule.name.contains("Transfer", ignoreCase = true) -> stringResource(R.string.flosi_rule_summary_transfer)
                    else -> null
                }

                conditionSummary?.let {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.Icon.small),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Priority badge (only show for non-default priority)
                if (rule.priority != 100) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.flosi_rule_priority, rule.priority),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // More actions menu - only show when rule is active
                if (rule.isActive) {
                    Box {
                        IconButton(
                            onClick = { showActionsMenu = true }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.flosi_more_actions)
                            )
                        }

                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false }
                        ) {
                            // Edit rule
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.flosi_full_edit_rule)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                                onClick = {
                                    showActionsMenu = false
                                    onEdit()
                                }
                            )

                            // Duplicate rule (opens the editor prefilled as a new rule)
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.flosi_full_duplicate_rule)) },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                },
                                onClick = {
                                    showActionsMenu = false
                                    onDuplicate()
                                }
                            )

                            // Apply to past transactions
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.flosi_full_apply_to_past_transactions)) },
                                leadingIcon = {
                                    Icon(Icons.Default.History, contentDescription = null)
                                },
                                onClick = {
                                    showActionsMenu = false
                                    onApplyToPast()
                                }
                            )

                            // Only show delete for custom rules
                            if (!rule.isSystemTemplate) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.flosi_full_delete_rule)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showActionsMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                Switch(
                    checked = rule.isActive,
                    onCheckedChange = onToggle
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.flosi_full_delete_rule)) },
            text = { Text(stringResource(R.string.flosi_rule_delete, displayRuleName(rule))) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.flosi_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.flosi_cancel))
                }
            }
        )
    }
}

@Composable
private fun BatchApplyDialog(
    rule: com.pennywiseai.tracker.domain.model.rule.TransactionRule,
    progress: Pair<Int, Int>?,
    result: BatchApplyResult?,
    dryRunResult: DryRunResult?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onPreview: () -> Unit,
    onApplyToAll: () -> Unit,
    onApplyToUncategorized: () -> Unit
) {
    val title = when {
        progress != null -> stringResource(R.string.flosi_rule_applying)
        isLoading && dryRunResult == null -> stringResource(R.string.flosi_rule_previewing)
        dryRunResult != null && result == null -> stringResource(R.string.flosi_rule_preview_name, displayRuleName(rule))
        result != null -> stringResource(R.string.flosi_rule_apply_past)
        else -> stringResource(R.string.flosi_rule_apply_past)
    }

    AlertDialog(
        onDismissRequest = {
            if (progress == null && !isLoading) {
                onDismiss()
            }
        },
        title = { Text(text = title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                when {
                    // Loading preview
                    isLoading && dryRunResult == null && progress == null && result == null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(stringResource(R.string.flosi_full_scanning_transactions), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Preview result (dry run) — before actual apply
                    dryRunResult != null && result == null && progress == null -> {
                        if (dryRunResult.totalMatched == 0) {
                            Text(
                                text = stringResource(R.string.flosi_rule_no_match, dryRunResult.totalScanned),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.flosi_rule_scanned, dryRunResult.totalScanned),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = pluralStringResource(R.plurals.flosi_rule_would_update, dryRunResult.totalWouldUpdate, dryRunResult.totalWouldUpdate),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            if (dryRunResult.totalWouldBlock > 0) {
                                Text(
                                    text = pluralStringResource(R.plurals.flosi_rule_would_block, dryRunResult.totalWouldBlock, dryRunResult.totalWouldBlock),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (dryRunResult.samples.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))
                                Text(
                                    text = stringResource(R.string.flosi_full_sample_changes),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                dryRunResult.samples.take(5).forEach { diff ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (diff.isBlock)
                                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(Spacing.sm)) {
                                            val orig = diff.original
                                            Text(
                                                text = "${orig.merchantName} - ${orig.amount}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (diff.isBlock) {
                                                Text(
                                                    text = stringResource(R.string.flosi_full_would_be_blocked),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            } else if (diff.modified != null) {
                                                val mod = diff.modified
                                                if (orig.category != mod.category) {
                                                    Text(
                                                        text = stringResource(R.string.flosi_rule_diff_category, localizedCategoryName(orig.category), localizedCategoryName(mod.category)),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                if (orig.merchantName != mod.merchantName) {
                                                    Text(
                                                        text = stringResource(R.string.flosi_rule_diff_merchant, orig.merchantName, mod.merchantName),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                if (orig.transactionType != mod.transactionType) {
                                                    Text(
                                                        text = stringResource(R.string.flosi_rule_diff_type, localizedRuleTransactionTypeLabel(orig.transactionType.name), localizedRuleTransactionTypeLabel(mod.transactionType.name)),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                if (orig.description != mod.description) {
                                                    Text(
                                                        text = stringResource(R.string.flosi_rule_diff_description, orig.description ?: stringResource(R.string.flosi_rule_none), mod.description ?: stringResource(R.string.flosi_rule_none)),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                if (diff.tagChanges.isNotEmpty()) {
                                                    Text(
                                                        text = stringResource(R.string.flosi_rule_diff_tags, diff.tagChanges.joinToString(", ")),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (dryRunResult.totalMatched > 5) {
                                    Text(
                                        text = pluralStringResource(R.plurals.flosi_rule_more, dryRunResult.totalMatched - 5, dryRunResult.totalMatched - 5),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Initial state — show options with preview button
                    progress == null && result == null -> {
                        Text(
                            text = stringResource(R.string.flosi_rule_confirm_apply, displayRuleName(rule)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.flosi_full_use_preview_to_see_what_would_change_before_applying),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Processing state
                    progress != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(
                                text = stringResource(R.string.flosi_rule_processing, progress.first, progress.second),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Result state
                    result != null -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (result.errors.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (result.errors.isEmpty())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = stringResource(R.string.flosi_full_completed),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

                            Text(
                                text = pluralStringResource(R.plurals.flosi_rule_processed, result.totalProcessed, result.totalProcessed),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = pluralStringResource(R.plurals.flosi_rule_updated, result.totalUpdated, result.totalUpdated),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            if (result.totalDeleted > 0) {
                                Text(
                                    text = pluralStringResource(R.plurals.flosi_rule_blocked, result.totalDeleted, result.totalDeleted),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (result.errors.isNotEmpty()) {
                                Text(
                                    text = pluralStringResource(R.plurals.flosi_rule_errors, result.errors.size, result.errors.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when {
                // Loading — no buttons
                isLoading -> {}

                // Preview result — show Apply or Cancel
                dryRunResult != null && result == null && progress == null -> {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.flosi_cancel)) }
                        if (dryRunResult.totalMatched > 0) {
                            TextButton(onClick = onApplyToUncategorized) { Text(stringResource(R.string.category_uncategorized)) }
                            TextButton(onClick = onApplyToAll) { Text(stringResource(R.string.flosi_full_apply_all)) }
                        }
                    }
                }

                // Initial state — show Preview + direct apply buttons
                progress == null && result == null -> {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.flosi_cancel)) }
                        OutlinedButton(onClick = onPreview) {
                            Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(Dimensions.Icon.small))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(stringResource(R.string.flosi_preview))
                        }
                    }
                }

                // Done — close button
                result != null -> {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.flosi_close)) }
                }
            }
        }
    )
}
