package cash.z.ecc.android.di

import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.ui.MainActivityModule
import cash.z.ecc.android.ui.detail.WalletDetailFragmentModule
import cash.z.ecc.android.ui.home.HomeFragmentModule
import cash.z.ecc.android.ui.receive.ReceiveFragmentModule
import cash.z.ecc.android.ui.send.SendFragmentModule
import cash.z.ecc.android.ui.setup.BackupFragmentModule
import cash.z.ecc.android.ui.setup.LandingFragmentModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,

        AppModule::class,

        // Activities
        MainActivityModule::class,

        // Fragments
        HomeFragmentModule::class,
        ReceiveFragmentModule::class,
        SendFragmentModule::class,
        WalletDetailFragmentModule::class,
        LandingFragmentModule::class,
        BackupFragmentModule::class
    ]
)
interface AppComponent : AndroidInjector<ZcashWalletApp> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<ZcashWalletApp>()
}