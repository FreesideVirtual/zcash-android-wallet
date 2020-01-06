package cash.z.ecc.android.di.module

import android.content.ClipboardManager
import android.content.Context
import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.di.component.MainActivitySubcomponent
import dagger.Module
import dagger.Provides
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
