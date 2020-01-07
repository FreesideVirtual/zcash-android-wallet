package cash.z.ecc.android.ui.receive

import androidx.lifecycle.ViewModel
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.ext.twig
import javax.inject.Inject

class ReceiveViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var synchronizer: Synchronizer

    suspend fun getAddress(): String = synchronizer.getAddress()

    override fun onCleared() {
        super.onCleared()
        twig("WalletDetailViewModel cleared!")
    }
}
