package cash.z.ecc.android.di.component

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import cash.z.ecc.android.di.annotation.ActivityScope
import cash.z.ecc.android.di.module.MainActivityModule
import cash.z.ecc.android.di.module.ViewModelsModule
import cash.z.ecc.android.ui.MainActivity
import cash.z.ecc.android.ui.detail.WalletDetailFragment
import cash.z.ecc.android.ui.home.HomeFragment
import cash.z.ecc.android.ui.receive.ReceiveFragment
import cash.z.ecc.android.ui.send.SendAddressFragment
import cash.z.ecc.android.ui.send.SendConfirmFragment
import cash.z.ecc.android.ui.send.SendFinalFragment
import cash.z.ecc.android.ui.send.SendMemoFragment
import cash.z.ecc.android.ui.setup.BackupFragment
import cash.z.ecc.android.ui.setup.LandingFragment
import dagger.BindsInstance
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [MainActivityModule::class, ViewModelsModule::class])
interface MainActivitySubcomponent {

    fun inject(activity: MainActivity)

    // Fragments
    fun inject(fragment: HomeFragment)
    fun inject(fragment: ReceiveFragment)
    fun inject(fragment: SendAddressFragment)
    fun inject(fragment: SendMemoFragment)
    fun inject(fragment: SendConfirmFragment)
    fun inject(fragment: SendFinalFragment)
    fun inject(fragment: WalletDetailFragment)
    fun inject(fragment: LandingFragment)
    fun inject(fragment: BackupFragment)

    fun viewModels(): ViewModelProvider
    fun viewModelFactory(): ViewModelProvider.Factory

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance activity: FragmentActivity): MainActivitySubcomponent
    }
}