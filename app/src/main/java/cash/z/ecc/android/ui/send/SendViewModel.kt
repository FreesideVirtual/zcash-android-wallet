package cash.z.ecc.android.ui.send

import androidx.lifecycle.ViewModel
import cash.z.ecc.android.lockbox.LockBox
import cash.z.ecc.android.ui.setup.WalletSetupViewModel
import cash.z.wallet.sdk.SdkSynchronizer
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.entity.PendingTransaction
import cash.z.wallet.sdk.ext.twig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SendViewModel @Inject constructor(var lockBox: LockBox) : ViewModel() {
    fun send(synchronizer: Synchronizer): Flow<PendingTransaction> {
        val keys = (synchronizer as SdkSynchronizer).rustBackend!!.deriveSpendingKeys(
            lockBox.getBytes(WalletSetupViewModel.LockBoxKey.SEED)!!
        )
        return synchronizer.sendToAddress(
            keys[0],
            zatoshiAmount,
            toAddress,
            memo
        ).onEach {
            twig(it.toString())
        }
    }

    var toAddress: String = ""
    var memo: String = ""
    var zatoshiAmount: Long = -1L
}