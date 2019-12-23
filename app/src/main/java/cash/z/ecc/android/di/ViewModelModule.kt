package cash.z.ecc.android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cash.z.ecc.android.di.annotation.ViewModelKey
import cash.z.ecc.android.ui.home.HomeViewModel
import cash.z.ecc.android.ui.send.SendViewModel
import cash.z.ecc.android.ui.setup.WalletSetupViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(implementation: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(WalletSetupViewModel::class)
    abstract fun bindWalletSetupViewModel(implementation: WalletSetupViewModel):  ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(implementation: HomeViewModel):  ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SendViewModel::class)
    abstract fun bindSendViewModel(implementation: SendViewModel):  ViewModel
}

@Singleton
class ViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?: creators.entries.firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value ?: throw IllegalArgumentException(
            "No map entry found for ${modelClass.canonicalName}." +
                    " Verify that this ViewModel has been added to the ViewModelModule."
        )
        @Suppress("UNCHECKED_CAST")
        return creator.get() as T
    }
}