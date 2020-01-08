package cash.z.ecc.android.ui.scan

import androidx.lifecycle.ViewModel
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.ext.twig
import javax.inject.Inject

class ScanViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var synchronizer: Synchronizer

    suspend fun isNotValid(address: String) = synchronizer.validateAddress(address).isNotValid

    override fun onCleared() {
        super.onCleared()
        twig("${javaClass.simpleName} cleared!")
    }

}
