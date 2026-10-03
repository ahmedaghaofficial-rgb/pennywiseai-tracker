package com.pennywiseai.tracker.ui.screens.rules

import com.pennywiseai.tracker.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.pennywiseai.tracker.ui.effects.overScrollVertical
import com.pennywiseai.tracker.data.database.entity.AccountBalanceEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pennywiseai.tracker.domain.model.rule.*
import com.pennywiseai.tracker.ui.components.CustomTitleTopAppBar
import com.pennywiseai.tracker.ui.components.FinancialAccountIdentity
import com.pennywiseai.tracker.ui.theme.Dimensions
import com.pennywiseai.tracker.ui.viewmodel.RulesViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.pennywiseai.tracker.ui.theme.Spacing
import java.util.UUID

/**
 * Transaction-type options for rule condition/action pickers: stored enum name → display label.
 * Labels mirror the [com.pennywiseai.tracker.data.database.entity.TransactionType] names (e.g.
 * EXPENSE → "Expense") so what the user picks matches what the rule stores and applies.
 */
internal val RULE_TRANSACTION_TYPE_OPTIONS: List<Pair<String, String>> = listOf(
    "INCOME" to "Income",
    "EXPENSE" to "Expense",
    "CREDIT" to "Credit",
    "TRANSFER" to "Transfer",
    "INVESTMENT" to "Investment"
)

/** User-facing label for a stored transaction-type value, falling back to the raw value. */
internal fun ruleTransactionTypeLabel(value: String): String =
    RULE_TRANSACTION_TYPE_OPTIONS.firstOrNull { it.first.equals(value, ignoreCase = true) }?.second
        ?: value

@Composable
private fun localizedRuleTransactionTypeLabel(value: String): String = when (value.uppercase()) {
    "INCOME" -> stringResource(R.string.flosi_rule_type_income)
    "EXPENSE" -> stringResource(R.string.flosi_rule_type_expense)
    "CREDIT" -> stringResource(R.string.flosi_rule_type_credit)
    "TRANSFER" -> stringResource(R.string.flosi_rule_type_transfer)
    "INVESTMENT" -> stringResource(R.string.flosi_rule_type_investment)
    else -> value
}

/** The operator a field should reset to when it becomes the condition's field. */
private fun TransactionField.defaultConditionOperator(): ConditionOperator = when (this) {
    TransactionField.AMOUNT -> ConditionOperator.LESS_THAN
    TransactionField.TRANSACTION_TIME -> ConditionOperator.LESS_THAN
    TransactionField.TRANSACTION_HOUR -> ConditionOperator.EQUALS
    TransactionField.TRANSACTION_DAY_OF_WEEK -> ConditionOperator.EQUALS
    TransactionField.TRANSACTION_DAY_OF_MONTH -> ConditionOperator.EQUALS
    TransactionField.TRANSACTION_DATE -> ConditionOperator.EQUALS
    TransactionField.ACCOUNT -> ConditionOperator.EQUALS
    TransactionField.TYPE -> ConditionOperator.EQUALS
    else -> ConditionOperator.CONTAINS
}

/**
 * Operators offered for a condition field, paired with their display label.
 *
 * The set and its order come from [supportedOperators] so the picker and the
 * rule importer can't drift apart; only the wording lives here, since the same
 * operator reads differently per field ("<" for an amount, "before" for a time).
 */
@Composable
@Composable
private fun conditionOperatorsForField(
    field: TransactionField
): List<Pair<ConditionOperator, String>> =
    supportedOperators(field).map { operator -> operator to operator.labelFor(field) }

@Composable
@Composable
private fun ConditionOperator.labelFor(field: TransactionField): String = when (field) {
    TransactionField.AMOUNT -> when (this) {
        ConditionOperator.LESS_THAN -> "<"
        ConditionOperator.GREATER_THAN -> ">"
        else -> "="
    }
    TransactionField.TRANSACTION_TIME -> when (this) {
        ConditionOperator.LESS_THAN -> stringResource(R.string.flosi_rule_before)
        ConditionOperator.GREATER_THAN -> stringResource(R.string.flosi_rule_after)
        ConditionOperator.GREATER_THAN_OR_EQUAL -> stringResource(R.string.flosi_rule_at_or_after)
        ConditionOperator.LESS_THAN_OR_EQUAL -> stringResource(R.string.flosi_rule_at_or_before)
        else -> stringResource(R.string.flosi_rule_exactly_at)
    }
    TransactionField.TRANSACTION_HOUR,
    TransactionField.TRANSACTION_DAY_OF_MONTH,
    TransactionField.TRANSACTION_DATE -> when (this) {
        ConditionOperator.LESS_THAN -> stringResource(R.string.flosi_rule_before)
        ConditionOperator.GREATER_THAN -> stringResource(R.string.flosi_rule_after)
        ConditionOperator.IN -> stringResource(R.string.flosi_rule_any_of)
        else -> stringResource(R.string.flosi_rule_is)
    }
    TransactionField.TYPE,
    TransactionField.TRANSACTION_DAY_OF_WEEK,
    TransactionField.ACCOUNT -> when (this) {
        ConditionOperator.NOT_EQUALS -> stringResource(R.string.flosi_rule_is_not)
        ConditionOperator.IN -> stringResource(R.string.flosi_rule_any_of)
        else -> stringResource(R.string.flosi_rule_is)
    }
    else -> when (this) {
        ConditionOperator.EQUALS -> stringResource(R.string.flosi_rule_equals)
        ConditionOperator.STARTS_WITH -> stringResource(R.string.flosi_rule_starts_with)
        else -> stringResource(R.string.flosi_rule_contains)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRuleScreen(
    onNavigateBack: () -> Unit,
    onSaveRule: (TransactionRule) -> Unit,
    existingRule: TransactionRule? = null,
    // True only when editing a saved rule. A duplicate passes a prefilled [existingRule]
    // (carrying a fresh id) but isEditing = false, so it saves as a brand-new rule.
    // Defaults to false: a non-null prefill must NOT imply edit, or a duplicate that
    // omits this flag would overwrite its source. Callers state edit intent explicitly.
    isEditing: Boolean = false,
    allAccounts: List<RulesViewModel.AccountInfo> = emptyList()
) {
    var ruleName by remember(existingRule) { mutableStateOf(existingRule?.name ?: "") }
    var description by remember(existingRule) { mutableStateOf(existingRule?.description ?: "") }

    // Initialize conditions list from existing rule or use single default condition
    var conditions by remember(existingRule) {
        mutableStateOf(existingRule?.conditions?.toMutableList() ?: mutableListOf(
            RuleCondition(
                field = TransactionField.AMOUNT,
                operator = ConditionOperator.LESS_THAN,
                value = ""
            )
        ))
    }

    // Initialize actions list from existing rule or use a single default action
    var actions by remember(existingRule) {
        mutableStateOf(
            existingRule?.actions?.takeIf { it.isNotEmpty() }
                ?: listOf(
                    RuleAction(
                        field = TransactionField.CATEGORY,
                        actionType = ActionType.SET,
                        value = ""
                    )
                )
        )
    }

    // Holds a pending switch-to-BLOCK while we confirm discarding the other actions.
    var pendingBlockAction by remember { mutableStateOf<RuleAction?>(null) }

    // Common presets for quick setup
    val commonPresets = listOf(
        "Block OTPs" to {
            ruleName = "Block OTP Messages"
            conditions = mutableListOf(
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.CONTAINS,
                    value = "OTP"
                )
            )
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.BLOCK,
                    value = ""
                )
            )
        },
        "Block Small Amounts" to {
            ruleName = "Block Small Transactions"
            conditions = mutableListOf(
                RuleCondition(
                    field = TransactionField.AMOUNT,
                    operator = ConditionOperator.LESS_THAN,
                    value = "10"
                )
            )
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.BLOCK,
                    value = ""
                )
            )
        },
        "Small amounts → Food" to {
            ruleName = "Small Food Payments"
            conditions = mutableListOf(
                RuleCondition(
                    field = TransactionField.AMOUNT,
                    operator = ConditionOperator.LESS_THAN,
                    value = "200"
                )
            )
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Food & Dining"
                )
            )
        },
        "Standardize Merchant" to {
            ruleName = "Standardize Merchant Name"
            conditions = mutableListOf(
                RuleCondition(
                    field = TransactionField.MERCHANT,
                    operator = ConditionOperator.CONTAINS,
                    value = "AMZN"
                )
            )
            actions = listOf(
                RuleAction(
                    field = TransactionField.MERCHANT,
                    actionType = ActionType.SET,
                    value = "Amazon"
                )
            )
        },
        "Mark as Income" to {
            ruleName = "Mark Credits as Income"
            conditions = mutableListOf(
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.CONTAINS,
                    value = "credited"
                )
            )
            actions = listOf(
                RuleAction(
                    field = TransactionField.TYPE,
                    actionType = ActionType.SET,
                    value = "INCOME"
                )
            )
        },
        "Daily Investment" to {
            ruleName = "Daily Investment"
            conditions = mutableListOf(
                RuleCondition(
                    field = TransactionField.TRANSACTION_TIME,
                    operator = ConditionOperator.GREATER_THAN_OR_EQUAL,
                    value = "09:00"
                ),
                RuleCondition(
                    field = TransactionField.TRANSACTION_TIME,
                    operator = ConditionOperator.LESS_THAN,
                    value = "09:30"
                )
            )
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Investments"
                )
            )
        }
    )

    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollBehaviorLarge = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviorLarge.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehaviorLarge,
                title = if (isEditing) "Edit Rule" else "Create Rule",
                hasBackButton = true,
                hasActionButton = true,
                navigationContent = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.flosi_close))
                    }
                },
                actionContent = {
                    TextButton(
                        onClick = {
                            // Validate: rule name + all conditions have values + all actions are valid
                            val areConditionsValid = conditions.isNotEmpty() &&
                                conditions.all { it.validate() }
                            val isActionValid = actions.isNotEmpty() && actions.all { it.validate() }
                            val isValid = ruleName.isNotBlank() && areConditionsValid && isActionValid

                            if (isValid) {
                                val rule = TransactionRule(
                                    id = existingRule?.id ?: UUID.randomUUID().toString(),
                                    name = ruleName,
                                    description = description.takeIf { it.isNotBlank() },
                                    priority = existingRule?.priority ?: 100,
                                    conditions = conditions.toList(),
                                    actions = actions,
                                    isActive = existingRule?.isActive ?: true,
                                    isSystemTemplate = existingRule?.isSystemTemplate ?: false,
                                    createdAt = existingRule?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSaveRule(rule)
                            }
                        },
                        enabled = ruleName.isNotBlank() &&
                                 conditions.isNotEmpty() &&
                                 conditions.all { it.validate() } &&
                                 actions.isNotEmpty() &&
                                 actions.all { it.validate() }
                    ) {
                        Text(stringResource(R.string.flosi_save))
                    }
                },
                hazeState = hazeState
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(Dimensions.Padding.content)
                .imePadding()
                .overScrollVertical()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Quick presets
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = stringResource(R.string.flosi_full_quick_templates),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        commonPresets.forEach { (label, action) ->
                            ElevatedAssistChip(
                                onClick = action,
                                label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }
            }

            // Rule name and description
            TextField(
                value = ruleName,
                onValueChange = { ruleName = it },
                label = { Text(stringResource(R.string.flosi_full_rule_name)) },
                placeholder = { Text(stringResource(R.string.flosi_full_e_g_food_expenses_under_200)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.flosi_description_optional)) },
                placeholder = { Text(stringResource(R.string.flosi_full_what_does_this_rule_do)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Conditions section (supports multiple)
            Card {
                Column(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.flosi_full_when_769bb1),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(
                            onClick = {
                                conditions = (conditions + RuleCondition(
                                    field = TransactionField.AMOUNT,
                                    operator = ConditionOperator.LESS_THAN,
                                    value = ""
                                )).toMutableList()
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(Dimensions.Icon.small))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(stringResource(R.string.flosi_full_add_condition))
                        }
                    }

                    // Display all conditions
                    conditions.forEachIndexed { index, condition ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.md),
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                // Header with logical-operator toggle and delete button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (index == 0) {
                                        Text(
                                            text = stringResource(R.string.flosi_full_condition),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        LogicalOperatorToggle(
                                            selected = condition.logicalOperator,
                                            onSelect = { newOp ->
                                                conditions = conditions.toMutableList().apply {
                                                    set(index, condition.copy(logicalOperator = newOp))
                                                }
                                            }
                                        )
                                    }
                                    if (conditions.size > 1) {
                                        IconButton(
                                            onClick = {
                                                conditions = conditions.toMutableList().apply { removeAt(index) }
                                            },
                                            modifier = Modifier.size(Dimensions.Component.minTouchTarget)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.flosi_full_remove_condition),
                                                modifier = Modifier.size(Dimensions.Icon.small),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                // Field selector
                                ConditionFieldSelector(
                                    condition = condition,
                                    onConditionChange = { newCondition ->
                                        conditions = conditions.toMutableList().apply {
                                            set(index, newCondition)
                                        }
                                    },
                                    allAccounts = allAccounts
                                )
                            }
                        }
                    }
                }
            }

            // Action section (supports multiple)
            Card {
                Column(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.flosi_full_then),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        // BLOCK drops the transaction, so it's terminal — no further
                        // actions can run alongside it. Hide "Add Action" while one exists.
                        if (actions.none { it.actionType == ActionType.BLOCK }) {
                            TextButton(
                                onClick = {
                                    actions = actions + RuleAction(
                                        field = TransactionField.CATEGORY,
                                        actionType = ActionType.SET,
                                        value = ""
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(Dimensions.Icon.small))
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(stringResource(R.string.flosi_full_add_action))
                            }
                        }
                    }

                    // Display all actions
                    actions.forEachIndexed { index, action ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.md),
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                // Header with delete button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (actions.size > 1) stringResource(R.string.flosi_rule_action_index, index + 1) else stringResource(R.string.flosi_full_action),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (actions.size > 1) {
                                        IconButton(
                                            onClick = {
                                                actions = actions.toMutableList().apply { removeAt(index) }
                                            },
                                            modifier = Modifier.size(Dimensions.Component.minTouchTarget)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.flosi_full_remove_action),
                                                modifier = Modifier.size(Dimensions.Icon.small),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                // Per-action editor
                                ActionEditor(
                                    action = action,
                                    onActionChange = { updated ->
                                        // BLOCK is terminal — it drops the transaction, so the other
                                        // actions can't run. If the user switches to BLOCK while other
                                        // actions exist, confirm before discarding them (no silent
                                        // data loss); otherwise apply the change directly.
                                        if (updated.actionType == ActionType.BLOCK && actions.size > 1) {
                                            pendingBlockAction = updated
                                        } else {
                                            actions = actions.toMutableList().apply { set(index, updated) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Preview
            val showPreview = ruleName.isNotBlank() &&
                             conditions.isNotEmpty() &&
                             conditions.all { it.validate() } &&
                             actions.isNotEmpty() &&
                             actions.all { it.validate() }
            if (showPreview) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Dimensions.Padding.content),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Text(
                            text = stringResource(R.string.flosi_full_rule_preview),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = buildString {
                                append(stringResource(R.string.flosi_rule_when))
                                conditions.forEachIndexed { index, condition ->
                                    if (index > 0) append(stringResource(R.string.flosi_rule_and))
                                    append(when(condition.field) {
                                        TransactionField.AMOUNT -> stringResource(R.string.flosi_rule_field_amount)
                                        TransactionField.TYPE -> stringResource(R.string.flosi_rule_field_type)
                                        TransactionField.CATEGORY -> stringResource(R.string.flosi_rule_field_category)
                                        TransactionField.MERCHANT -> stringResource(R.string.flosi_rule_field_merchant)
                                        TransactionField.NARRATION -> stringResource(R.string.flosi_rule_field_description)
                                        TransactionField.SMS_TEXT -> stringResource(R.string.flosi_rule_field_sms)
                                        TransactionField.BANK_NAME -> stringResource(R.string.flosi_rule_field_bank)
                                        TransactionField.TRANSACTION_TIME -> stringResource(R.string.flosi_rule_field_time)
                                        TransactionField.TRANSACTION_HOUR -> stringResource(R.string.flosi_rule_field_hour)
                                        TransactionField.TRANSACTION_DAY_OF_WEEK -> stringResource(R.string.flosi_rule_field_day_week)
                                        TransactionField.TRANSACTION_DAY_OF_MONTH -> stringResource(R.string.flosi_rule_field_day_month)
                                        TransactionField.TRANSACTION_DATE -> stringResource(R.string.flosi_rule_field_date)
                                        TransactionField.ACCOUNT -> stringResource(R.string.flosi_rule_field_account)
                                        TransactionField.TAGS -> stringResource(R.string.flosi_rule_field_tags)
                                    })
                                    append(" ")
                                    append(when(condition.operator) {
                                        ConditionOperator.LESS_THAN -> stringResource(R.string.flosi_rule_before)
                                        ConditionOperator.GREATER_THAN -> stringResource(R.string.flosi_rule_after)
                                        ConditionOperator.LESS_THAN_OR_EQUAL -> stringResource(R.string.flosi_rule_at_or_before)
                                        ConditionOperator.GREATER_THAN_OR_EQUAL -> stringResource(R.string.flosi_rule_at_or_after)
                                        ConditionOperator.EQUALS -> stringResource(R.string.flosi_rule_is)
                                        ConditionOperator.CONTAINS -> stringResource(R.string.flosi_rule_contains)
                                        ConditionOperator.STARTS_WITH -> stringResource(R.string.flosi_rule_starts_with)
                                        ConditionOperator.IN -> stringResource(R.string.flosi_rule_any_of)
                                        ConditionOperator.NOT_EQUALS -> stringResource(R.string.flosi_rule_is_not)
                                        else -> stringResource(R.string.flosi_rule_matches)
                                    })
                                    append(" ")
                                    val dayNames = mapOf(
                                        "1" to "Mon", "2" to "Tue", "3" to "Wed", "4" to "Thu",
                                        "5" to "Fri", "6" to "Sat", "7" to "Sun"
                                    )
                                    when {
                                        condition.field == TransactionField.TYPE -> {
                                            append(localizedRuleTransactionTypeLabel(condition.value))
                                        }
                                        condition.field == TransactionField.TRANSACTION_DAY_OF_WEEK -> {
                                            append(condition.value.split(",").joinToString(", ") { dayNames[it.trim()] ?: it })
                                        }
                                        condition.field == TransactionField.ACCOUNT -> {
                                            val parts = condition.value.split("||")
                                            if (parts.size == 2) {
                                                append(AccountBalanceEntity.accountLabel(parts[0], parts[1]))
                                            } else {
                                                append(condition.value)
                                            }
                                        }
                                        else -> append(condition.value)
                                    }
                                }
                                append(", ")
                                actions.forEachIndexed { actionIndex, action ->
                                    if (actionIndex > 0) append(stringResource(R.string.flosi_rule_action_join))
                                    if (action.actionType == ActionType.BLOCK) {
                                        append(stringResource(R.string.flosi_rule_block_action))
                                    } else if (action.field == TransactionField.TAGS) {
                                        append(if (action.actionType == ActionType.ADD_TAG) stringResource(R.string.flosi_rule_add_tag) else stringResource(R.string.flosi_rule_remove_tag))
                                        append(action.value)
                                    } else {
                                        val fieldName = when (action.field) {
                                            TransactionField.CATEGORY -> stringResource(R.string.flosi_rule_field_category)
                                            TransactionField.MERCHANT -> stringResource(R.string.flosi_rule_field_merchant)
                                            TransactionField.TYPE -> stringResource(R.string.flosi_rule_field_type)
                                            TransactionField.NARRATION -> stringResource(R.string.flosi_rule_field_description)
                                            TransactionField.BANK_NAME -> stringResource(R.string.flosi_rule_field_account)
                                            else -> stringResource(R.string.flosi_rule_field_field)
                                        }
                                        // Show user-friendly labels for transaction types in actions too
                                        val displayValue = if (action.field == TransactionField.TYPE) {
                                            localizedRuleTransactionTypeLabel(action.value)
                                        } else {
                                            action.value
                                        }
                                        when (action.actionType) {
                                            ActionType.APPEND -> append(stringResource(R.string.flosi_rule_append, displayValue, fieldName))
                                            ActionType.PREPEND -> append(stringResource(R.string.flosi_rule_prepend, displayValue, fieldName))
                                            ActionType.CLEAR -> append(stringResource(R.string.flosi_rule_clear, fieldName))
                                            else -> append(stringResource(R.string.flosi_rule_set, fieldName, displayValue))
                                        }
                                    }
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

    // Confirm before BLOCK discards the other (possibly filled-in) actions.
    if (pendingBlockAction != null) {
        AlertDialog(
            onDismissRequest = { pendingBlockAction = null },
            title = { Text(stringResource(R.string.flosi_full_block_transaction)) },
            text = {
                Text(
                    stringResource(R.string.flosi_rule_block_warning)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    actions = listOf(pendingBlockAction!!)
                    pendingBlockAction = null
                }) { Text(stringResource(R.string.flosi_full_block_remove)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingBlockAction = null }) { Text(stringResource(R.string.flosi_cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionFieldSelector(
    condition: RuleCondition,
    onConditionChange: (RuleCondition) -> Unit,
    allAccounts: List<RulesViewModel.AccountInfo> = emptyList()
) {
    var fieldDropdownExpanded by remember { mutableStateOf(false) }

    // Field selector
    ExposedDropdownMenuBox(
        expanded = fieldDropdownExpanded,
        onExpandedChange = { fieldDropdownExpanded = !fieldDropdownExpanded }
    ) {
        val fieldOptions = listOf(
            TransactionField.AMOUNT to stringResource(R.string.flosi_rule_field_amount),
            TransactionField.TYPE to stringResource(R.string.flosi_rule_field_type),
            TransactionField.CATEGORY to stringResource(R.string.flosi_rule_field_category),
            TransactionField.MERCHANT to stringResource(R.string.flosi_rule_field_merchant),
            TransactionField.SMS_TEXT to stringResource(R.string.flosi_rule_field_sms),
            TransactionField.BANK_NAME to stringResource(R.string.flosi_rule_field_bank),
            TransactionField.TRANSACTION_TIME to stringResource(R.string.flosi_rule_field_time),
            TransactionField.TRANSACTION_HOUR to stringResource(R.string.flosi_rule_field_hour),
            TransactionField.TRANSACTION_DAY_OF_WEEK to stringResource(R.string.flosi_rule_field_day_week),
            TransactionField.TRANSACTION_DAY_OF_MONTH to stringResource(R.string.flosi_rule_field_day_month),
            TransactionField.TRANSACTION_DATE to stringResource(R.string.flosi_rule_field_date),
            TransactionField.ACCOUNT to stringResource(R.string.flosi_rule_field_account)
        )
        TextField(
            value = fieldOptions.firstOrNull { it.first == condition.field }?.second ?: stringResource(R.string.flosi_rule_field_amount),
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.flosi_full_field)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fieldDropdownExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = fieldDropdownExpanded,
            onDismissRequest = { fieldDropdownExpanded = false }
        ) {
            fieldOptions.forEach { (field, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        // Reset value AND operator: the previous operator may be invalid for the
                        // new field (e.g. keeping "<" from Amount when switching to Transaction
                        // Type, which only supports is / is not).
                        onConditionChange(
                            condition.copy(
                                field = field,
                                value = "",
                                operator = field.defaultConditionOperator()
                            )
                        )
                        fieldDropdownExpanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Operator selector
    val operators = conditionOperatorsForField(condition.field)

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        operators.forEach { (op, label) ->
            FilterChip(
                selected = condition.operator == op,
                onClick = { onConditionChange(condition.copy(operator = op)) },
                label = { Text(label) }
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Value input
    when (condition.field) {
        TransactionField.TYPE -> {
            Text(
                text = stringResource(R.string.flosi_full_select_transaction_type),
                style = MaterialTheme.typography.bodySmall
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                RULE_TRANSACTION_TYPE_OPTIONS.forEach { (type, displayLabel) ->
                    FilterChip(
                        selected = condition.value.equals(type, ignoreCase = true),
                        onClick = { onConditionChange(condition.copy(value = type)) },
                        label = {
                            Text(localizedRuleTransactionTypeLabel(type), style = MaterialTheme.typography.bodySmall)
                        }
                    )
                }
            }
        }

        TransactionField.TRANSACTION_DAY_OF_WEEK -> {
            val days = listOf(
                "1" to "Mon", "2" to "Tue", "3" to "Wed", "4" to "Thu",
                "5" to "Fri", "6" to "Sat", "7" to "Sun"
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (condition.operator == ConditionOperator.IN ||
                    condition.operator == ConditionOperator.NOT_IN
                ) {
                    val selectedDays = condition.value.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                    days.forEach { (value, label) ->
                        FilterChip(
                            selected = value in selectedDays,
                            onClick = {
                                val newSet = if (value in selectedDays) selectedDays - value else selectedDays + value
                                onConditionChange(condition.copy(value = newSet.sorted().joinToString(",")))
                            },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                } else {
                    days.forEach { (value, label) ->
                        FilterChip(
                            selected = condition.value == value,
                            onClick = { onConditionChange(condition.copy(value = value)) },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
        }

        TransactionField.TRANSACTION_DAY_OF_MONTH -> {
            TextField(
                value = condition.value,
                onValueChange = { onConditionChange(condition.copy(value = it)) },
                label = { Text(stringResource(R.string.flosi_full_day_1_31)) },
                placeholder = { Text(stringResource(R.string.flosi_full_e_g_1)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        TransactionField.TRANSACTION_TIME -> {
            var showTimePicker by remember { mutableStateOf(false) }
            val initialHour = condition.value.split(":").getOrNull(0)?.toIntOrNull() ?: 9
            val initialMinute = condition.value.split(":").getOrNull(1)?.toIntOrNull() ?: 0
            val timePickerState = rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute
            )

            TextField(
                value = condition.value,
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.flosi_full_time_hh_mm)) },
                placeholder = { Text(stringResource(R.string.flosi_full_tap_to_select_time)) },
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = stringResource(R.string.flosi_full_pick_time))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val formatted = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                            onConditionChange(condition.copy(value = formatted))
                            showTimePicker = false
                        }) { Text(stringResource(R.string.flosi_ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.flosi_cancel)) }
                    },
                    text = { TimePicker(state = timePickerState) }
                )
            }
        }

        TransactionField.TRANSACTION_HOUR -> {
            TextField(
                value = condition.value,
                onValueChange = { onConditionChange(condition.copy(value = it)) },
                label = { Text(stringResource(R.string.flosi_full_hour_0_23)) },
                placeholder = { Text(stringResource(R.string.flosi_full_e_g_9)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        TransactionField.TRANSACTION_DATE -> {
            TextField(
                value = condition.value,
                onValueChange = { onConditionChange(condition.copy(value = it)) },
                label = { Text(stringResource(R.string.flosi_full_date_yyyy_mm_dd)) },
                placeholder = { Text(stringResource(R.string.flosi_full_e_g_2026_03_21)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        TransactionField.ACCOUNT -> {
            // Account dropdown picker
            var accountDropdownExpanded by remember { mutableStateOf(false) }

            val selectedAccount = allAccounts.firstOrNull {
                "${it.bankName}||${it.accountLast4}" == condition.value
            }

            val displayText = selectedAccount?.let {
                it.displayName
            } ?: if (condition.value.isNotBlank()) {
                // Show raw value if account no longer exists
                condition.value
            } else {
                stringResource(R.string.flosi_rule_select_account)
            }

            Column {
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    TextField(
                        value = displayText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.flosi_account)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        allAccounts.forEach { account ->
                            val key = "${account.bankName}||${account.accountLast4}"
                            val accountTypeLabel = when {
                                account.isCreditCard -> "Credit"
                                account.accountType != null -> account.accountType
                                else -> ""
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        FinancialAccountIdentity(
                                            bankName = account.bankName,
                                            accountLast4 = account.accountLast4
                                        )
                                        if (accountTypeLabel.isNotBlank()) {
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(accountTypeLabel, style = MaterialTheme.typography.labelSmall) },
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onConditionChange(condition.copy(value = key))
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (allAccounts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.flosi_full_no_accounts_found_add_an_account_first),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
        }

        else -> {
            TextField(
                value = condition.value,
                onValueChange = { onConditionChange(condition.copy(value = it)) },
                label = { Text(stringResource(R.string.flosi_full_value)) },
                placeholder = {
                    Text(
                        when(condition.field) {
                            TransactionField.AMOUNT -> "e.g., 200"
                            TransactionField.MERCHANT -> "e.g., Swiggy"
                            TransactionField.SMS_TEXT -> "e.g., salary"
                            TransactionField.CATEGORY -> "e.g., Food & Dining"
                            TransactionField.BANK_NAME -> "e.g., HDFC Bank"
                            else -> "Enter value"
                        }
                    )
                },
                keyboardOptions = if (condition.field == TransactionField.AMOUNT) {
                    KeyboardOptions(keyboardType = KeyboardType.Number)
                } else {
                    KeyboardOptions.Default
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogicalOperatorToggle(
    selected: LogicalOperator,
    onSelect: (LogicalOperator) -> Unit
) {
    val options = listOf(LogicalOperator.AND, LogicalOperator.OR)
    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, op ->
            SegmentedButton(
                selected = selected == op,
                onClick = { onSelect(op) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = {
                    Text(
                        text = op.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
    }
}

@Composable
@Composable
private fun actionTypeLabel(type: ActionType): String = when (type) {
    ActionType.BLOCK -> stringResource(R.string.flosi_rule_action_block)
    ActionType.SET -> stringResource(R.string.flosi_rule_action_set)
    ActionType.APPEND -> stringResource(R.string.flosi_rule_action_append)
    ActionType.PREPEND -> stringResource(R.string.flosi_rule_action_prepend)
    ActionType.CLEAR -> stringResource(R.string.flosi_rule_action_clear)
    ActionType.ADD_TAG -> stringResource(R.string.flosi_rule_action_add_tag)
    ActionType.REMOVE_TAG -> stringResource(R.string.flosi_rule_action_remove_tag)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionEditor(
    action: RuleAction,
    onActionChange: (RuleAction) -> Unit
) {
    var actionTypeDropdownExpanded by remember { mutableStateOf(false) }
    var actionFieldDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Action type selector
        ExposedDropdownMenuBox(
            expanded = actionTypeDropdownExpanded,
            onExpandedChange = { actionTypeDropdownExpanded = !actionTypeDropdownExpanded }
        ) {
            TextField(
                value = actionTypeLabel(action.actionType),
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.flosi_full_action_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionTypeDropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = actionTypeDropdownExpanded,
                onDismissRequest = { actionTypeDropdownExpanded = false }
            ) {
                // Offer exactly what the engine can carry out on this field. The
                // list used to be hardcoded to Set/Clear, which hid the Append and
                // Prepend actions the engine has always supported on merchant and
                // description (#747).
                val fieldTypes = supportedActionTypes(action.field).map { it to actionTypeLabel(it) }
                (listOf(ActionType.BLOCK to actionTypeLabel(ActionType.BLOCK)) + fieldTypes).forEach { (type, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onActionChange(
                                action.copy(
                                    actionType = type,
                                    value = if (type == ActionType.BLOCK) "" else action.value
                                )
                            )
                            actionTypeDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Show message for BLOCK action or field selector for others
        if (action.actionType == ActionType.BLOCK) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(R.string.flosi_full_transactions_matching_this_rule_will_be_blocked_and_not_saved),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else {
            // Action field selector for non-BLOCK actions
            ExposedDropdownMenuBox(
                expanded = actionFieldDropdownExpanded,
                onExpandedChange = { actionFieldDropdownExpanded = !actionFieldDropdownExpanded }
            ) {
                TextField(
                    value = when(action.field) {
                        TransactionField.CATEGORY -> "Category"
                        TransactionField.MERCHANT -> "Merchant Name"
                        TransactionField.TYPE -> "Transaction Type"
                        TransactionField.NARRATION -> "Description"
                        TransactionField.BANK_NAME -> "Account"
                        TransactionField.TAGS -> "Tags"
                        else -> "Field"
                    },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.flosi_full_action)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionFieldDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = actionFieldDropdownExpanded,
                    onDismissRequest = { actionFieldDropdownExpanded = false }
                ) {
                    listOf(
                        TransactionField.CATEGORY to stringResource(R.string.flosi_rule_field_category),
                        TransactionField.MERCHANT to "Merchant Name",
                        TransactionField.TYPE to stringResource(R.string.flosi_rule_field_type),
                        TransactionField.NARRATION to "Description",
                        TransactionField.BANK_NAME to stringResource(R.string.flosi_rule_field_account),
                        TransactionField.TAGS to "Tags"
                    ).forEach { (field, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                // Keep the action type one the engine can carry out on the
                                // new field (SET on TAGS, or ADD_TAG on CATEGORY, would be dead).
                                val types = supportedActionTypes(field)
                                val actionType = if (action.actionType in types) action.actionType else types.first()
                                onActionChange(action.copy(field = field, actionType = actionType, value = ""))
                                actionFieldDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Dynamic value input based on selected action field
            when (action.field) {
                TransactionField.CATEGORY -> {
                    // Category chips and input
                    val commonCategories = listOf(
                        "Food & Dining", "Transportation", "Shopping",
                        "Bills & Utilities", "Entertainment", "Healthcare",
                        "Investments", "Others"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        commonCategories.forEach { category ->
                            FilterChip(
                                selected = action.value == category,
                                onClick = { onActionChange(action.copy(value = category)) },
                                label = { Text(category, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    TextField(
                        value = action.value,
                        onValueChange = { onActionChange(action.copy(value = it)) },
                        label = { Text(stringResource(R.string.flosi_category_name)) },
                        placeholder = { Text(stringResource(R.string.flosi_full_e_g_rent)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                TransactionField.TYPE -> {
                    // Transaction type chips with user-friendly labels
                    Text(
                        text = stringResource(R.string.flosi_full_select_transaction_type),
                        style = MaterialTheme.typography.bodySmall
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RULE_TRANSACTION_TYPE_OPTIONS.forEach { (type, displayLabel) ->
                            FilterChip(
                                selected = action.value.equals(type, ignoreCase = true),
                                onClick = { onActionChange(action.copy(value = type)) },
                                label = {
                                    Text(
                                        displayLabel,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            )
                        }
                    }
                }

                TransactionField.MERCHANT -> {
                    // Merchant name input with common suggestions
                    val commonMerchants = listOf(
                        "Amazon", "Swiggy", "Zomato", "Uber",
                        "Netflix", "Google", "Flipkart"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        commonMerchants.forEach { merchant ->
                            ElevatedAssistChip(
                                onClick = { onActionChange(action.copy(value = merchant)) },
                                label = { Text(merchant, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    TextField(
                        value = action.value,
                        onValueChange = { onActionChange(action.copy(value = it)) },
                        label = { Text(stringResource(R.string.flosi_full_merchant_name)) },
                        placeholder = { Text(stringResource(R.string.flosi_full_e_g_amazon)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                TransactionField.NARRATION -> {
                    // Description/Narration input
                    TextField(
                        value = action.value,
                        onValueChange = { onActionChange(action.copy(value = it)) },
                        label = { Text(stringResource(R.string.flosi_description)) },
                        placeholder = { Text(stringResource(R.string.flosi_full_e_g_monthly_subscription_payment)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                TransactionField.BANK_NAME -> {
                    // Account / bank the transaction belongs to.
                    TextField(
                        value = action.value,
                        onValueChange = { onActionChange(action.copy(value = it)) },
                        label = { Text(stringResource(R.string.flosi_full_account_bank_name)) },
                        placeholder = { Text(stringResource(R.string.flosi_full_e_g_hdfc_bank)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                TransactionField.TAGS -> {
                    TextField(
                        value = action.value,
                        onValueChange = { onActionChange(action.copy(value = it)) },
                        label = { Text(stringResource(R.string.flosi_full_tag_name)) },
                        placeholder = { Text(stringResource(R.string.flosi_full_e_g_swiggy)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                else -> {
                    // Generic text input for other fields
                    TextField(
                        value = action.value,
                        onValueChange = { onActionChange(action.copy(value = it)) },
                        label = { Text(stringResource(R.string.flosi_full_value)) },
                        placeholder = { Text(stringResource(R.string.flosi_full_enter_value)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}