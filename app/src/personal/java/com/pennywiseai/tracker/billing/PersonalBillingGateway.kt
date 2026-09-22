package com.pennywiseai.tracker.billing

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Personal build billing adapter.
 *
 * This flavor is intentionally non-commercial: every existing gated feature
 * is unlocked, there is no Play Billing dependency, and no purchase flow can
 * be launched. Keeping this behind the existing billing interfaces means the
 * shared app code remains unchanged and a future paid flavor can coexist.
 */
@Singleton
class PersonalBillingGateway @Inject constructor() : PurchaseGateway {

    override val isPro: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    override val products: StateFlow<List<ProProduct>> =
        MutableStateFlow(emptyList<ProProduct>()).asStateFlow()

    override suspend fun refresh(): PurchaseResult = PurchaseResult.Unsupported

    override suspend fun launchPurchase(
        activity: Activity,
        product: ProProduct,
    ): PurchaseResult = PurchaseResult.Unsupported
}
