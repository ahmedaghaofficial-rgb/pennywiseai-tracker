package com.pennywiseai.tracker.ui.icons

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pennywiseai.tracker.R

/** Only the visible label is translated; database and parser category keys stay stable. */
@Composable
fun localizedCategoryName(name: String): String {
    val resource = categoryNameResource(name) ?: return name // Preserve user-created names.
    return stringResource(resource)
}

/** Resource for built-in display labels; the stored category key stays unchanged. */
@StringRes
fun categoryNameResource(name: String): Int? = when (name) {
        "Food & Dining" -> R.string.category_food_dining
        "Groceries" -> R.string.category_groceries
        "Transportation" -> R.string.category_transportation
        "Shopping" -> R.string.category_shopping
        "Bills & Utilities" -> R.string.category_bills_utilities
        "Entertainment" -> R.string.category_entertainment
        "Healthcare" -> R.string.category_healthcare
        "Investments" -> R.string.category_investments
        "Banking" -> R.string.category_banking
        "Personal Care" -> R.string.category_personal_care
        "Education" -> R.string.category_education
        "Mobile" -> R.string.category_mobile
        "Fitness" -> R.string.category_fitness
        "Insurance" -> R.string.category_insurance
        "Tax" -> R.string.category_tax
        "Bank Charges" -> R.string.category_bank_charges
        "Credit Card Payment" -> R.string.category_credit_card_payment
        "Salary" -> R.string.category_salary
        "Income" -> R.string.category_income
        "Travel" -> R.string.category_travel
        "Others" -> R.string.category_others
        "Uncategorized" -> R.string.category_uncategorized
        else -> null
}
