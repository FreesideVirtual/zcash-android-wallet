package cash.z.ecc.android.ui.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.lockbox.LockBox
import cash.z.ecc.android.ui.setup.WalletSetupViewModel
import cash.z.wallet.sdk.Initializer
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.entity.PendingTransaction
import cash.z.wallet.sdk.ext.ZcashSdk
import cash.z.wallet.sdk.ext.twig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class SendViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var lockBox: LockBox

    @Inject
    lateinit var synchronizer: Synchronizer

    @Inject
    lateinit var initializer: Initializer

    fun send(): Flow<PendingTransaction> {
        val memoToSend = if (includeFromAddress) "$memo\nsent from\n$fromAddress" else memo
        val keys = initializer.deriveSpendingKeys(
            lockBox.getBytes(WalletSetupViewModel.LockBoxKey.SEED)!!
        )
        return synchronizer.sendToAddress(
            keys[0],
            zatoshiAmount,
            toAddress,
            memoToSend
        ).onEach {
            twig(it.toString())
        }
    }

    fun validate() = flow<String?> {

        when {
            synchronizer.validateAddress(toAddress).isNotValid -> {
                emit("Please enter a valid address")
            }
            zatoshiAmount < ZcashSdk.MINERS_FEE_ZATOSHI -> {
                emit("Please enter a larger amount")
            }
            synchronizer.getAddress() == toAddress -> {
                emit("That appears to be your address!")
            }
            else -> emit(null)
        }
    }

    fun afterInitFromAddress(block: () -> Unit) {
        viewModelScope.launch {
            fromAddress = synchronizer.getAddress()
            block()
        }
    }

    var fromAddress: String = ""
    var toAddress: String = ""
    var memo: String = ""
    var zatoshiAmount: Long = -1L
    var includeFromAddress: Boolean = false
        set(value) {
            require(!value || (value && !fromAddress.isNullOrEmpty())) {
                "Error: from address was empty while attempting to include it in the memo. Verify" +
                        " that initFromAddress() has previously been called on this viewmodel."
            }
            field = value
        }
}