package com.pennywiseai.tracker.core.localization

import android.content.Context
import com.pennywiseai.tracker.R
import com.pennywiseai.tracker.data.database.entity.TransactionEntity

// The hash identifies generated adjustments without changing the transaction schema.
private const val HASH_PREFIX = "manual_balance_adjustment_"
const val BALANCE_ADJUSTMENT_STORAGE_NAME = "Balance adjustment"
private const val ARABIC_NAME = "تعديل الرصيد"
private const val ENGLISH_DESCRIPTION = "Untracked amount so the app matches the bank's reported balance of "
private const val ARABIC_DESCRIPTION = "مبلغ غير مسجل لمطابقة رصيد البنك المعلن: "

fun storedBalanceAdjustmentDescription(reportedBalance: String): String =
    ENGLISH_DESCRIPTION + reportedBalance

private fun isGeneratedAdjustment(transaction: TransactionEntity): Boolean =
    transaction.transactionHash.startsWith(HASH_PREFIX)

fun localizedTransactionMerchant(context: Context, transaction: TransactionEntity): String =
    if (isGeneratedAdjustment(transaction) &&
        transaction.merchantName in setOf(BALANCE_ADJUSTMENT_STORAGE_NAME, ARABIC_NAME)
    ) {
        AppLocaleController.wrap(context).getString(R.string.flosi_balance_adjustment)
    } else {
        transaction.merchantName
    }

internal fun balanceAdjustmentAmount(description: String): String? =
    when {
        description.startsWith(ENGLISH_DESCRIPTION) -> description.removePrefix(ENGLISH_DESCRIPTION)
        description.startsWith(ARABIC_DESCRIPTION) -> description.removePrefix(ARABIC_DESCRIPTION)
        else -> null
    }?.takeIf { it.isNotBlank() }

fun localizedTransactionDescription(context: Context, transaction: TransactionEntity): String? {
    val description = transaction.description ?: return null
    if (!isGeneratedAdjustment(transaction)) return description
    val reportedBalance = balanceAdjustmentAmount(description) ?: return description
    return AppLocaleController.wrap(context)
        .getString(R.string.flosi_balance_adjustment_description, reportedBalance)
}
