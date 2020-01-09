package cash.z.ecc.android.ui.home

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import cash.z.wallet.sdk.SdkSynchronizer
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.Synchronizer.Status.DISCONNECTED
import cash.z.wallet.sdk.Synchronizer.Status.SYNCED
import cash.z.wallet.sdk.ext.ZcashSdk.MINERS_FEE_ZATOSHI
import cash.z.wallet.sdk.ext.ZcashSdk.ZATOSHI_PER_ZEC
import cash.z.wallet.sdk.ext.twig
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

class HomeViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var synchronizer: Synchronizer

    lateinit var uiModels: Flow<UiModel>

    var initialized = false

    fun initialize(
        typedChars: Flow<Char>
    ) {
        twig("init called")
        if (initialized) {
            twig("Warning already initialized HomeViewModel. Ignoring call to initialize.")
            return
        }
        val zec = typedChars.scan("0") { acc, c ->
            when {
                // no-op cases
                acc == "0" && c == '0'
                        || (c == '<' && acc == "0")
                        || (c == '.' && acc.contains('.')) -> {twig("triggered: 1  acc: $acc  c: $c  $typedChars ")
                    acc
               }
                c == '<' && acc.length <= 1 -> {twig("triggered: 2 $typedChars")
                    "0"
                }
                c == '<' -> {twig("triggered: 3")
                    acc.substring(0, acc.length - 1)
                }
                acc == "0" && c != '.' -> {twig("triggered: 4 $typedChars")
                    c.toString()
                }
                else -> {twig("triggered: 5  $typedChars")
                    "$acc$c"
                }
            }
        }
        uiModels = synchronizer.run {
            combine(status, progress, balances, zec) { s, p, b, z->
                UiModel(s, p, b.availableZatoshi, b.totalZatoshi, z)
            }
        }.conflate()
    }

    override fun onCleared() {
        super.onCleared()
        twig("HomeViewModel cleared!")
    }

    suspend fun refreshBalance() {
        (synchronizer as SdkSynchronizer).refreshBalance()
    }

    @Parcelize
    data class UiModel( // <- THIS ERROR IS AN IDE BUG WITH PARCELIZE
        val status: Synchronizer.Status = DISCONNECTED,
        val progress: Int = 0,
        val availableBalance: Long = -1L,
        val totalBalance: Long = -1L,
        val pendingSend: String = "0"
    ): Parcelable {
        // Note: the wallet is effectively empty if it cannot cover the miner's fee
        val hasFunds: Boolean get() = availableBalance > (MINERS_FEE_ZATOSHI.toDouble() / ZATOSHI_PER_ZEC) // 0.0001
        val hasBalance: Boolean get() = totalBalance > (MINERS_FEE_ZATOSHI.toDouble() / ZATOSHI_PER_ZEC) // 0.0001
        val isSynced: Boolean get() = status == SYNCED
        val isSendEnabled: Boolean get() = isSynced && hasFunds
    }
}
