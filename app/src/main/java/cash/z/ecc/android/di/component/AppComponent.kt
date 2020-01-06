package cash.z.ecc.android.di.component

import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.di.module.AppModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    // Subcomponents
    fun mainActivityComponent(): MainActivitySubcomponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: ZcashWalletApp): AppComponent
    }
}