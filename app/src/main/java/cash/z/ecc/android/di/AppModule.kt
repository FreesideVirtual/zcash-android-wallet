package cash.z.ecc.android.di

import android.content.Context
import cash.z.ecc.android.ZcashWalletApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AppBindingModule::class, ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideAppContext(): Context = ZcashWalletApp.instance
}
