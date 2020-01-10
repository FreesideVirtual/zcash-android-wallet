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
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.entity.*
import cash.z.wallet.sdk.ext.toAbbreviatedAddress
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.twig
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
        binding.backButtonHitArea.setOnClickListener {
            onExit()
        }
        binding.textConfirmation.text =
            "Sending ${sendViewModel.zatoshiAmount.convertZatoshiToZecString(8)} ZEC to ${sendViewModel.toAddress.toAbbreviatedAddress()}"
        sendViewModel.memo?.trim()?.isNotEmpty()?.let { hasMemo ->
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
        val id = pendingTransaction?.id ?: -1
        var isSending = true
        val message = when {
            pendingTransaction == null -> "Transaction not found"
            pendingTransaction.isMined() -> "Transaction Mined (id: $id)!\n\nSEND COMPLETE".also { isSending = false }
            pendingTransaction.isSubmitSuccess() -> "Successfully submitted transaction!\nAwaiting confirmation . . ."
            pendingTransaction.isFailedEncoding() -> "ERROR: failed to encode transaction! (id: $id)".also { isSending = false }
            pendingTransaction.isFailedSubmit() -> "ERROR: failed to submit transaction! (id: $id)".also { isSending = false }
            pendingTransaction.isCreated() -> "Transaction creation complete! (id: $id)"
            pendingTransaction.isCreating() -> "Creating transaction . . ."
            else -> "Transaction updated!".also { twig("Unhandled TX state: $pendingTransaction") }
        }
        twig("Pending TX Updated: $message")
        binding.textStatus.apply {
            text = "$text\n$message"
        }
        binding.backButton.goneIf(!binding.textStatus.text.toString().contains("Awaiting"))
        binding.buttonNext.goneIf(isSending)
        binding.progressHorizontal.goneIf(!isSending)

        if (pendingTransaction?.isSubmitSuccess() == true) {
            sendViewModel.reset()
        }
    }

    private fun onExit() {
        mainActivity?.navController?.popBackStack(R.id.nav_send_address, true)
    }
}