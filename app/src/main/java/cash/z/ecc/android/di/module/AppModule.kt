package cash.z.ecc.android.di.module

import android.content.ClipboardManager
import android.content.Context
import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.di.component.InitializerSubcomponent
import cash.z.ecc.android.di.component.MainActivitySubcomponent
import cash.z.ecc.android.di.component.SynchronizerSubcomponent
import cash.z.wallet.sdk.Initializer
import dagger.Module
import dagger.Provides
import dagger.Reusable
import javax.inject.Singleton

@Module(subcomponents = [MainActivitySubcomponent::class])
class AppModule {

    @Provides
    @Singleton
    fun provideAppContext(): Context = ZcashWalletApp.instance

    @Provides
    @Singleton
    fun provideClipboard(context: Context) =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}
