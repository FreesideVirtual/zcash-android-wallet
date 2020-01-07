package cash.z.ecc.android.di.module

import android.content.Context
import cash.z.wallet.sdk.Initializer
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
class InitializerModule {
    private val host = "lightd-main.zecwallet.co"
    private val port = 443

    @Provides
    @Reusable
    fun provideInitializer(appContext: Context) = Initializer(appContext, host, port)
}
