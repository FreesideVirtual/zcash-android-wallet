package cash.z.ecc.android.di.module


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cash.z.ecc.android.di.annotation.ActivityScope
import cash.z.ecc.android.di.annotation.ViewModelKey
import cash.z.ecc.android.di.viewmodel.ViewModelFactory
import cash.z.ecc.android.ui.home.HomeViewModel
import cash.z.ecc.android.ui.send.SendViewModel
import cash.z.ecc.android.ui.setup.WalletSetupViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {

    //
    // Activity View Models
    //

    @ActivityScope
    @Binds
    @IntoMap
    @ViewModelKey(WalletSetupViewModel::class)
    abstract fun bindWalletSetupViewModel(implementation: WalletSetupViewModel): ViewModel

    @ActivityScope
    @Binds
    @IntoMap
    @ViewModelKey(SendViewModel::class)
    abstract fun bindSendViewModel(implementation: SendViewModel): ViewModel


    //
    // Fragment View Models
    //

    @ActivityScope
    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(implementation: HomeViewModel): ViewModel


    //
    // View Model Helpers
    //

    @ActivityScope
    @Binds
    abstract fun bindViewModelFactory(implementation: ViewModelFactory): ViewModelProvider.Factory

}