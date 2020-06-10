package cash.z.ecc.android.di.component

import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.di.annotation.ActivityScope
import cash.z.ecc.android.di.annotation.SynchronizerScope
import cash.z.ecc.android.di.module.InitializerModule
import cash.z.ecc.android.sdk.Initializer
import dagger.BindsInstance
import dagger.Subcomponent

@SynchronizerScope
@Subcomponent(modules = [InitializerModule::class])
interface InitializerSubcomponent {

    fun initializer(): Initializer
    fun birthdayStore(): Initializer.WalletBirthdayStore

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance birthdayStore: Initializer.WalletBirthdayStore = Initializer.DefaultBirthdayStore(ZcashWalletApp.instance)): InitializerSubcomponent
    }
}