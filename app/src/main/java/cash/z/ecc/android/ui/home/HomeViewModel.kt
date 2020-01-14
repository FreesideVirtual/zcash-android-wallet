package cash.z.ecc.android.ui.home

import androidx.lifecycle.ViewModel
import cash.z.wallet.sdk.SdkSynchronizer
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.Synchronizer.Status.DISCONNECTED
import cash.z.wallet.sdk.Synchronizer.Status.SYNCED
import cash.z.wallet.sdk.block.CompactBlockProcessor
import cash.z.wallet.sdk.ext.ZcashSdk.MINERS_FEE_ZATOSHI
import cash.z.wallet.sdk.ext.ZcashSdk.ZATOSHI_PER_ZEC
import cash.z.wallet.sdk.ext.twig
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.math.roundToInt

class HomeViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var synchronizer: Synchronizer

    lateinit var uiModels: Flow<UiModel>

    private val _typedChars = ConflatedBroadcastChannel<Char>()
    private val typedChars = _typedChars.asFlow()

    var initialized = false

    fun initializeMaybe() {
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
            combine(status, processorInfo, balances, zec) { s, p, b, z->
                UiModel(s, p, b.availableZatoshi, b.totalZatoshi, z)
            }
        }.conflate()
    }

    override fun onCleared() {
        super.onCleared()
        twig("HomeViewModel cleared!")
    }

    suspend fun onChar(c: Char) {
        _typedChars.send(c)
    }

    suspend fun refreshBalance() {
        (synchronizer as SdkSynchronizer).refreshBalance()
    }

    data class UiModel( // <- THIS ERROR IS AN IDE BUG WITH PARCELIZE
        val status: Synchronizer.Status = DISCONNECTED,
        val processorInfo: CompactBlockProcessor.ProcessorInfo = CompactBlockProcessor.ProcessorInfo(),
        val availableBalance: Long = -1L,
        val totalBalance: Long = -1L,
        val pendingSend: String = "0"
    ) {
        // Note: the wallet is effectively empty if it cannot cover the miner's fee
        val hasFunds: Boolean get() = availableBalance > (MINERS_FEE_ZATOSHI.toDouble() / ZATOSHI_PER_ZEC) // 0.0001
        val hasBalance: Boolean get() = totalBalance > (MINERS_FEE_ZATOSHI.toDouble() / ZATOSHI_PER_ZEC) // 0.0001
        val isSynced: Boolean get() = status == SYNCED
        val isSendEnabled: Boolean get() = isSynced && hasFunds

        // Processor Info
        val isDownloading: Boolean
            get() = status != SYNCED
                    && processorInfo.lastDownloadedHeight < processorInfo.lastDownloadRange.last
        val isScanning: Boolean
            get() = status != SYNCED
                    && processorInfo.lastScannedHeight < processorInfo.lastScanRange.last
                    && processorInfo.lastScannedHeight > processorInfo.lastScanRange.first
        val isValidating: Boolean
            get() = (status != SYNCED)
                    && (!isScanning && !isDownloading)
        val downloadProgress: Int get() {
            return processorInfo.run {
                if (lastDownloadRange.isEmpty()) {
                    100
                } else {
                    twig("NUMERATOR: $lastDownloadedHeight - ${lastDownloadRange.first} + 1 = ${lastDownloadedHeight - lastDownloadRange.first + 1} block(s) downloaded")
                    twig("DENOMINATOR: ${lastDownloadRange.last} - ${lastDownloadRange.first} + 1 = ${lastDownloadRange.last - lastDownloadRange.first + 1} block(s) to download")
                    val progress =
                        (((lastDownloadedHeight - lastDownloadRange.first + 1).coerceAtLeast(0).toFloat() / (lastDownloadRange.last - lastDownloadRange.first + 1)) * 100.0f).coerceAtMost(
                            100.0f
                        ).roundToInt()
                    twig("RESULT: $progress")
                    progress
                }
            }
        }
        val scanProgress: Int get() {
            return processorInfo.run {
                if (lastScanRange.isEmpty()) {
                    100
                } else {
                    twig("NUMERATOR: ${lastScannedHeight - lastScanRange.first + 1} block(s) scanned")
                    twig("DENOMINATOR: ${lastScanRange.last - lastScanRange.first + 1} block(s) to scan")
                    val progress = (((lastScannedHeight - lastScanRange.first + 1).coerceAtLeast(0).toFloat() / (lastScanRange.last - lastScanRange.first + 1)) * 100.0f).coerceAtMost(100.0f).roundToInt()
                    twig("RESULT: $progress")
                    progress
                }
            }
        }
        val totalProgress: Float get() {
            val downloadWeighted = 0.40f * (downloadProgress.toFloat() / 100.0f).coerceAtMost(1.0f)
            val scanWeighted = 0.60f * (scanProgress.toFloat() / 100.0f).coerceAtMost(1.0f)
            return downloadWeighted.coerceAtLeast(0.0f) + scanWeighted.coerceAtLeast(0.0f)
        }
    }
}
