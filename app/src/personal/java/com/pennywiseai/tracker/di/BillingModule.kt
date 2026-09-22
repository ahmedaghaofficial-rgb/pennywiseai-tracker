package com.pennywiseai.tracker.di

import com.pennywiseai.tracker.billing.EntitlementSource
import com.pennywiseai.tracker.billing.PersonalBillingGateway
import com.pennywiseai.tracker.billing.PurchaseGateway
import com.pennywiseai.tracker.billing.PurchaseLauncher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Personal flavor: all current gated features are unlocked, with no billing rail. */
@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    @Singleton
    abstract fun bindPurchaseGateway(impl: PersonalBillingGateway): PurchaseGateway

    @Binds
    @Singleton
    abstract fun bindEntitlementSource(impl: PersonalBillingGateway): EntitlementSource

    @Binds
    @Singleton
    abstract fun bindPurchaseLauncher(impl: PersonalBillingGateway): PurchaseLauncher
}
