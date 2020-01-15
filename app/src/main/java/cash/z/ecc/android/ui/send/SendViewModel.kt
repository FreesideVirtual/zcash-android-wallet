package cash.z.ecc.android.ui.send

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.feedback.Feedback
import cash.z.ecc.android.feedback.Feedback.Keyed
import cash.z.ecc.android.feedback.Feedback.TimeMetric
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.feedback.Report.MetricType
import cash.z.ecc.android.feedback.Report.MetricType.*
import cash.z.ecc.android.lockbox.LockBox
import cash.z.ecc.android.ui.setup.WalletSetupViewModel
import cash.z.wallet.sdk.Initializer
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.annotation.OpenForTesting
import cash.z.wallet.sdk.entity.*
import cash.z.wallet.sdk.ext.ZcashSdk
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.twig
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendViewModel @Inject constructor() : ViewModel() {

    private val metrics = mutableMapOf<String, TimeMetric>()

    @Inject
    lateinit var lockBox: LockBox

    @Inject
    lateinit var synchronizer: Synchronizer

    @Inject
    lateinit var initializer: Initializer

    @Inject
    lateinit var feedback: Feedback

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
    val isShielded get() = toAddress.startsWith("z")
    
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

    suspend fun validateAddress(address: String): Synchronizer.AddressType =
        synchronizer.validateAddress(address)

    fun validate(maxZatoshi: Long?) = flow<String?> {

        when {
            synchronizer.validateAddress(toAddress).isNotValid -> {
                emit("Please enter a valid address")
            }
            zatoshiAmount < ZcashSdk.MINERS_FEE_ZATOSHI -> {
                emit("Too little! Please enter at least 0.0001")
            }
            maxZatoshi != null && zatoshiAmount > maxZatoshi -> {
                emit( "Too much! Please enter no more than ${maxZatoshi.convertZatoshiToZecString(8)}")
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

    fun reset() {
        fromAddress = ""
        toAddress = ""
        memo = ""
        zatoshiAmount = -1L
        includeFromAddress = false
    }

    fun updateMetrics(tx: PendingTransaction) {
        try {
            when {
                tx.isMined() -> TRANSACTION_SUBMITTED to TRANSACTION_MINED by tx.id
                tx.isSubmitSuccess() -> TRANSACTION_CREATED to TRANSACTION_SUBMITTED by tx.id
                tx.isCreated() -> TRANSACTION_INITIALIZED to TRANSACTION_CREATED by tx.id
                tx.isCreating() -> +TRANSACTION_INITIALIZED by tx.id
                else -> null
            }?.let { metricId ->
                report(metricId)
            }
        } catch (t: Throwable) {
            Crashlytics.logException(RuntimeException("Error while updating Metrics", t))
        }
    }

    fun report(metricId: String?) {
        metrics[metricId]?.let { metric ->
            metric.takeUnless { (it.elapsedTime ?: 0) <= 0L }?.let {
                viewModelScope.launch {
                    withContext(IO) {
                        feedback.report(metric)

                        // does this metric complete another metric?
                        metricId!!.toRelatedMetricId().let { relatedId ->
                            metrics[relatedId]?.let { relatedMetric ->
                                // then remove the related metric, itself. And the relation.
                                metrics.remove(relatedMetric.toMetricIdFor(metricId!!.toTxId()))
                                metrics.remove(relatedId)
                            }
                        }

                        // remove all top-level metrics
                        if (metric.key == Report.MetricType.TRANSACTION_MINED.key) metrics.remove(metricId)
                    }
                }
            }
        }
    }

    private operator fun MetricType.unaryPlus(): TimeMetric = TimeMetric(key, description).markTime()
    private infix fun TimeMetric.by(txId: Long) = this.toMetricIdFor(txId).also { metrics[it] = this }
    private infix fun Pair<MetricType, MetricType>.by(txId: Long): String? {
        val startMetric = first.toMetricIdFor(txId).let { metricId ->
            metrics[metricId].also { if (it == null) println("Warning no start metric for id: $metricId") }
        }
        return startMetric?.endTime?.let { startMetricEndTime ->
                TimeMetric(second.key, second.description, mutableListOf(startMetricEndTime))
                    .markTime().let { endMetric ->
                        endMetric.toMetricIdFor(txId).also { metricId ->
                            metrics[metricId] = endMetric
                            metrics[metricId.toRelatedMetricId()] = startMetric
                        }
                    }
            }

    }

    private fun Keyed<String>.toMetricIdFor(id: Long): String = "$id.$key"
    private fun String.toRelatedMetricId(): String = "$this.related"
    private fun String.toTxId(): Long = split('.').first().toLong()
}








