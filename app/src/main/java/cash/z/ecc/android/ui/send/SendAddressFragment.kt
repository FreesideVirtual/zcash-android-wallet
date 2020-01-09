package cash.z.ecc.android.ui.send

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendAddressBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.ext.*
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.ext.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SendAddressFragment : BaseFragment<FragmentSendAddressBinding>(),
    ClipboardManager.OnPrimaryClipChangedListener {

    val sendViewModel: SendViewModel by activityViewModel()

    override fun inflate(inflater: LayoutInflater): FragmentSendAddressBinding =
        FragmentSendAddressBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNext.setOnClickListener {
            onSubmit()
        }
        binding.backButtonHitArea.onClickNavTo(R.id.action_nav_send_address_to_nav_home)
        binding.textBannerAction.setOnClickListener {
            onPaste()
        }
        binding.textBannerMessage.setOnClickListener {
            onPaste()
        }

        // Apply View Model
        if (sendViewModel.zatoshiAmount > 0L) {
            sendViewModel.zatoshiAmount.convertZatoshiToZecString(8).let { amount ->
                binding.inputZcashAmount.setText(amount)
                binding.textAmount.text = "Sending $amount ZEC"
            }
        } else {
            binding.inputZcashAmount.setText(null)
        }
        if (!sendViewModel.toAddress.isNullOrEmpty()){
            binding.textAmount.text = "Send to ${sendViewModel.toAddress.toAbbreviatedAddress()}"
            binding.inputZcashAddress.setText(sendViewModel.toAddress)
        } else {
            binding.inputZcashAddress.setText(null)
        }

        binding.inputZcashAddress.onEditorActionDone(::onSubmit)

        binding.imageScanQr.onClickNavTo(R.id.action_nav_send_address_to_nav_scan)
    }


    private fun onSubmit(unused: EditText? = null) {
        sendViewModel.toAddress = binding.inputZcashAddress.text.toString()
        binding.inputZcashAmount.convertZecToZatoshi()?.let { sendViewModel.zatoshiAmount = it }
        sendViewModel.validate().onFirstWith(resumedScope) {
            if (it == null) {
                mainActivity?.navController?.navigate(R.id.action_nav_send_address_to_send_memo)
            } else {
                resumedScope.launch {
                    binding.textAddressError.text = it
                    delay(1500L)
                    binding.textAddressError.text =  ""
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity?.clipboard?.addPrimaryClipChangedListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity?.clipboard?.removePrimaryClipChangedListener(this)
    }

    override fun onResume() {
        super.onResume()
        updateClipboardBanner()
    }

    override fun onPrimaryClipChanged() {
        twig("clipboard changed!")
        updateClipboardBanner()
    }

    private fun updateClipboardBanner() {
        binding.groupBanner.goneIf(loadAddressFromClipboard() == null)
    }

    private fun onPaste() {
        mainActivity?.clipboard?.let { clipboard ->
            if (clipboard.hasPrimaryClip()) {
                binding.inputZcashAddress.setText(clipboard.text())
            }
        }
    }

    private fun loadAddressFromClipboard(): String? {
        mainActivity?.clipboard?.apply {
            if (hasPrimaryClip()) {
                text()?.let { text ->
                    if (text.startsWith("zs") && text.length > 70) {
                        return@loadAddressFromClipboard text.toString()
                    }
                    // treat t-addrs differently in the future
                    if (text.startsWith("t1") && text.length > 32) {
                        return@loadAddressFromClipboard text.toString()
                    }
                }
            }
        }
        return null
    }

    private fun ClipboardManager.text(): CharSequence =
        primaryClip!!.getItemAt(0).coerceToText(mainActivity)
}