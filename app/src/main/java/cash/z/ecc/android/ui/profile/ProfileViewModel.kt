package cash.z.ecc.android.ui.profile

import androidx.lifecycle.ViewModel
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.ext.twig
import javax.inject.Inject

class ProfileViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var synchronizer: Synchronizer

    suspend fun getAddress(): String = synchronizer.getAddress()

    override fun onCleared() {
        super.onCleared()
        twig("ProfileViewModel cleared!")
    }
}
