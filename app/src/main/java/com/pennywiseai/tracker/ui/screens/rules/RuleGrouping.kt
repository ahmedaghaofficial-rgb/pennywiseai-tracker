package com.pennywiseai.tracker.ui.screens.rules

import com.pennywiseai.tracker.domain.model.rule.ActionType
import com.pennywiseai.tracker.domain.model.rule.TransactionField
import com.pennywiseai.tracker.domain.model.rule.TransactionRule

/** UI grouping uses stable action keys, never the user-editable rule name. */
internal enum class RuleGroup { DAILY, INCOME, RECURRING, BANKING, HEALTH, OTHER }

internal fun ruleGroup(rule: TransactionRule): RuleGroup {
    val setActions = rule.actions.filter { it.actionType == ActionType.SET }
    if (setActions.any { it.field == TransactionField.TYPE && it.value.equals("INCOME", ignoreCase = true) }) {
        return RuleGroup.INCOME
    }
    if (setActions.any { it.field == TransactionField.TYPE &&
            (it.value.equals("TRANSFER", ignoreCase = true) || it.value.equals("INVESTMENT", ignoreCase = true)) }) {
        return RuleGroup.BANKING
    }
    val categories = setActions.filter { it.field == TransactionField.CATEGORY }.map { it.value }.toSet()
    return when {
        categories.any { it in setOf("Food & Dining", "Groceries", "Transportation", "Fuel") } -> RuleGroup.DAILY
        categories.any { it in setOf("Salary", "Income", "Cashback") } -> RuleGroup.INCOME
        categories.any { it in setOf("Bills & Utilities", "Rent", "Subscriptions") } -> RuleGroup.RECURRING
        categories.any { it in setOf("Investments", "Banking", "Credit Card Payment", "Transfer") } -> RuleGroup.BANKING
        "Healthcare" in categories -> RuleGroup.HEALTH
        else -> RuleGroup.OTHER
    }
}
