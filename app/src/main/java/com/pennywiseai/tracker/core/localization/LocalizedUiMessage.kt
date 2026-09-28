package com.pennywiseai.tracker.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pennywiseai.tracker.R

/** Translate known validation and result messages without changing ViewModel state or stored data. */
@Composable
fun localizedUiMessage(message: String): String = when (message) {
    "Amount is required" -> stringResource(R.string.validation_amount_required)
    "Invalid amount" -> stringResource(R.string.validation_invalid_amount)
    "Amount must be greater than 0" -> stringResource(R.string.validation_positive_amount)
    "Merchant/Description is required" -> stringResource(R.string.validation_merchant_required)
    "Too short" -> stringResource(R.string.validation_too_short)
    "Category is required" -> stringResource(R.string.validation_category_required)
    "Category name is required" -> stringResource(R.string.validation_category_name_required)
    "Service name is required" -> stringResource(R.string.validation_service_required)
    "Select both a From and To account" -> stringResource(R.string.validation_transfer_accounts)
    "From and To accounts must be different" -> stringResource(R.string.validation_different_accounts)
    "Both accounts must use the same currency" -> stringResource(R.string.validation_same_currency)
    "Failed to save transaction" -> stringResource(R.string.validation_save_transaction)
    "Failed to save transfer" -> stringResource(R.string.validation_save_transfer)
    "Failed to save subscription" -> stringResource(R.string.validation_save_subscription)
    "System categories cannot be edited" -> stringResource(R.string.validation_system_category_edit)
    "System categories cannot be deleted" -> stringResource(R.string.validation_system_category_delete)
    "Category updated successfully" -> stringResource(R.string.validation_category_updated)
    "Category created successfully" -> stringResource(R.string.validation_category_created)
    "Category deleted successfully" -> stringResource(R.string.validation_category_deleted)
    "Cannot delete this category" -> stringResource(R.string.validation_cannot_delete_category)
    else -> message // Keep unexpected errors intact for diagnosis.
}
