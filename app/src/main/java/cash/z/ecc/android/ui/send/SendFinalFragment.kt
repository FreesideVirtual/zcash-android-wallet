package cash.z.ecc.android.ui.send

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendFinalBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.feedback.Feedback
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.feedback.Report.MetricType.*
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.entity.*
import cash.z.wallet.sdk.ext.toAbbreviatedAddress
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.twig
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random

class SendFinalFragment : BaseFragment<FragmentSendFinalBinding>() {

    val sendViewModel: SendViewModel by activityViewModel()

    override fun inflate(inflater: LayoutInflater): FragmentSendFinalBinding =
        FragmentSendFinalBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNext.setOnClickListener {
            onExit()
        }
        binding.buttonRetry.setOnClickListener {
            onRetry()
        }
        binding.backButtonHitArea.setOnClickListener {
            onExit()
        }
        binding.textConfirmation.text =
            "Sending ${sendViewModel.zatoshiAmount.convertZatoshiToZecString(8)} ZEC to ${sendViewModel.toAddress.toAbbreviatedAddress()}"
        sendViewModel.memo.trim().isNotEmpty().let { hasMemo ->
            binding.radioIncludeAddress.isChecked = hasMemo
            binding.radioIncludeAddress.goneIf(!hasMemo)
        }
        mainActivity?.preventBackPress(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity?.apply {
            sendViewModel.send().onEach {
                onPendingTxUpdated(it)
            }.launchIn(mainActivity?.lifecycleScope!!)
        }
    }

    override fun onResume() {
        super.onResume()
        flow {
            val max = binding.progressHorizontal.max - 1
            var progress = 0
            while (progress < max) {
                emit(progress)
                delay(Random.nextLong(1000))
                progress++
            }
        }.onEach {
            binding.progressHorizontal.progress = it
        }.launchIn(resumedScope)
    }

    private fun onPendingTxUpdated(pendingTransaction: PendingTransaction?) {
        try {
            if (pendingTransaction != null) sendViewModel.updateMetrics(pendingTransaction)
            val id = pendingTransaction?.id ?: -1
            var isSending = true
            var isFailure = false
            val message = when {
                pendingTransaction == null -> "Transaction not found"
                pendingTransaction.isMined() -> "Transaction Mined!\n\nSEND COMPLETE".also { isSending = false }
                pendingTransaction.isSubmitSuccess() -> "Successfully submitted transaction!\nAwaiting confirmation . . ."
                pendingTransaction.isFailedEncoding() -> "ERROR: failed to encode transaction! (id: $id)".also { isSending = false; isFailure = true }
                pendingTransaction.isFailedSubmit() -> "ERROR: failed to submit transaction! (id: $id)".also { isSending = false; isFailure = true }
                pendingTransaction.isCreated() -> "Transaction creation complete!"
                pendingTransaction.isCreating() -> "Creating transaction . . ."
                else -> "Transaction updated!".also { twig("Unhandled TX state: $pendingTransaction") }
            }

            // TODO: make this error tracking easier to use and more spiffy
            if (pendingTransaction?.isFailedSubmit() == true) {
                sendViewModel.feedback.report(Report.Send.SubmitFailure(pendingTransaction?.errorCode, pendingTransaction?.errorMessage))
            }
            if (pendingTransaction?.isFailedEncoding() == true) {
                sendViewModel.feedback.report(Report.Send.EncodingFailure(pendingTransaction?.errorCode, pendingTransaction?.errorMessage))
            }

            twig("Pending TX (id: ${pendingTransaction?.id} Updated with message: $message")
            binding.textStatus.apply {
                text = "$message"
            }
            binding.backButton.goneIf(!binding.textStatus.text.toString().contains("Awaiting"))
            binding.buttonNext.goneIf((pendingTransaction?.isSubmitSuccess() != true) && (pendingTransaction?.isCreated() != true) && !isFailure)
            binding.buttonNext.text =  if (isSending) "Done" else "Finished"
            binding.buttonRetry.goneIf(!isFailure)
            binding.progressHorizontal.goneIf(!isSending)


            if (pendingTransaction?.isSubmitSuccess() == true) {
                sendViewModel.reset()
            }
        } catch(t: Throwable) {
            val message = "ERROR: error while handling pending transaction update! $t"
            twig(message)
            Crashlytics.log(message)
            Crashlytics.logException(t)
        }
    }

    private fun onExit() {
        mainActivity?.navController?.popBackStack(R.id.nav_home, false)
    }

    private fun onRetry() {
        mainActivity?.navController?.popBackStack(R.id.nav_send_address, false)
    }

}
