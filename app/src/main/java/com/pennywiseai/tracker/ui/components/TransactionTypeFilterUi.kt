package com.pennywiseai.tracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pennywiseai.tracker.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import com.pennywiseai.tracker.presentation.common.TransactionTypeFilter

@Composable
fun TransactionTypeFilter.shortLabel(): String = when (this) {
    TransactionTypeFilter.ALL -> stringResource(R.string.flosi_all)
    TransactionTypeFilter.INCOME -> stringResource(R.string.flosi_income)
    TransactionTypeFilter.EXPENSE -> stringResource(R.string.flosi_expense)
    TransactionTypeFilter.CREDIT -> stringResource(R.string.flosi_credit)
    TransactionTypeFilter.TRANSFER -> stringResource(R.string.flosi_transfer)
    TransactionTypeFilter.INVESTMENT -> stringResource(R.string.flosi_invest)
}

fun TransactionTypeFilter.filterIcon(): ImageVector = when (this) {
    TransactionTypeFilter.ALL -> Icons.AutoMirrored.Filled.ReceiptLong
    TransactionTypeFilter.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
    TransactionTypeFilter.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown
    TransactionTypeFilter.CREDIT -> Icons.Default.CreditCard
    TransactionTypeFilter.TRANSFER -> Icons.Default.SwapHoriz
    TransactionTypeFilter.INVESTMENT -> Icons.AutoMirrored.Filled.ShowChart
}
