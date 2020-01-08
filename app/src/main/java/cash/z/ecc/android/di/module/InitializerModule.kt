package cash.z.ecc.android.di.module

import android.content.Context
import cash.z.wallet.sdk.Initializer
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
class InitializerModule {
    private val host = "lightwalletd.z.cash"
    private val port = 9067

    @Provides
    @Reusable
    fun provideInitializer(appContext: Context) = Initializer(appContext, host, port)
}
