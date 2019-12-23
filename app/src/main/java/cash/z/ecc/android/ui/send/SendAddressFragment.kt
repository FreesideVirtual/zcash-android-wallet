package cash.z.ecc.android.ui.send

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendAddressBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.ext.onClickNavBack
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.twig
import dagger.Module
import dagger.android.ContributesAndroidInjector

class SendAddressFragment : BaseFragment<FragmentSendAddressBinding>(),
    ClipboardManager.OnPrimaryClipChangedListener {

    override fun inflate(inflater: LayoutInflater): FragmentSendAddressBinding =
        FragmentSendAddressBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNext.setOnClickListener {
            onAddAddress()
        }
        binding.backButtonHitArea.onClickNavBack()
        binding.textBannerAction.setOnClickListener {
            onPaste()
        }
        binding.textBannerMessage.setOnClickListener {
            onPaste()
        }
        binding.textAmount.text = "Sending ${mainActivity?.sendViewModel?.zatoshiAmount.convertZatoshiToZecString(8)} ZEC"
        binding.inputZcashAddress.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onAddAddress()
                true
            } else {
                false
            }
        }
    }

    private fun onAddAddress() {
        mainActivity?.sendViewModel?.toAddress = binding.inputZcashAddress.text.toString()
        mainActivity?.navController?.navigate(R.id.action_nav_send_address_to_send_memo)
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


@Module
abstract class SendAddressFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): SendAddressFragment
}
