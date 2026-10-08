package com.pennywiseai.tracker.ui.screens.rules

import com.pennywiseai.tracker.domain.model.rule.ActionType
import com.pennywiseai.tracker.domain.model.rule.ConditionOperator
import com.pennywiseai.tracker.domain.model.rule.RuleAction
import com.pennywiseai.tracker.domain.model.rule.RuleCondition
import com.pennywiseai.tracker.domain.model.rule.TransactionField
import com.pennywiseai.tracker.domain.model.rule.TransactionRule
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleGroupingTest {
    private fun rule(name: String, field: TransactionField, value: String) = TransactionRule(
        name = name,
        conditions = listOf(RuleCondition(TransactionField.AMOUNT, ConditionOperator.LESS_THAN, "200")),
        actions = listOf(RuleAction(field, ActionType.SET, value))
    )

    @Test fun `quick presets stay in the same section after translation or renaming`() {
        for ((field, value, expected) in listOf(
            Triple(TransactionField.CATEGORY, "Food & Dining", RuleGroup.DAILY),
            Triple(TransactionField.TYPE, "INCOME", RuleGroup.INCOME),
            Triple(TransactionField.CATEGORY, "Investments", RuleGroup.BANKING)
        )) {
            assertEquals(expected, ruleGroup(rule("English display name", field, value)))
            assertEquals(expected, ruleGroup(rule("اسم عربي مختلف", field, value)))
        }
    }

    @Test fun `recurring templates use stable actions and conditions across display names`() {
        for (category in listOf("Housing", "EMI")) {
            assertEquals(RuleGroup.RECURRING, ruleGroup(rule("اسم مختلف", TransactionField.CATEGORY, category)))
        }
        val subscription = rule("اسم اشتراك", TransactionField.CATEGORY, "Entertainment").copy(
            conditions = listOf(RuleCondition(
                TransactionField.SMS_TEXT,
                ConditionOperator.REGEX_MATCHES,
                "(?i)(subscription|recurring|auto-debit|mandate)"
            ))
        )
        assertEquals(RuleGroup.RECURRING, ruleGroup(subscription))
        assertEquals(RuleGroup.OTHER, ruleGroup(rule("ترفيه", TransactionField.CATEGORY, "Entertainment")))
    }
}
