package cash.z.ecc.android.ui.send

import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.forEach
import androidx.core.widget.doAfterTextChanged
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.ext.*
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.feedback.Report.Funnel.Send
import cash.z.ecc.android.feedback.Report.Tap.*
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.block.CompactBlockProcessor.WalletBalance
import cash.z.wallet.sdk.ext.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SendFragment : BaseFragment<FragmentSendBinding>(),
    ClipboardManager.OnPrimaryClipChangedListener {
    override val screen = Report.Screen.SEND

    private var maxZatoshi: Long? = null
    private var useShieldedFunds: Boolean = true
        set(value) {
            // if we're switching to shielded then no need to prevent it
            // otherwise, do not switch to transparent unless the user agrees to reset the memo
            if (value || resetMemo()) {
                field = value
                sendViewModel.useShieldedFunds = value
                applyMemo()
            }
        }

    val sendViewModel: SendViewModel by activityViewModel()

    val pasteLimit = 20

    override fun inflate(inflater: LayoutInflater): FragmentSendBinding =
        FragmentSendBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButtonHitArea.onClickNavTo(R.id.action_nav_send_to_nav_home) { tapped(SEND_BACK) }
        binding.sendButtonHitArea.setOnClickListener {
            onSubmit().also { tapped(SEND_NEXT) }
        }
//        binding.textBannerAction.setOnClickListener {
//            onPaste().also { tapped(SEND_PASTE) }
//        }
//        binding.textBannerMessage.setOnClickListener {
//            onPaste().also { tapped(SEND_PASTE) }
//        }
        binding.textPaste.setOnClickListener {
            onPaste().also { tapped(SEND_PASTE) }
        }
        binding.textMax.setOnClickListener {
            onMax().also { tapped(SEND_MAX) }
        }

        binding.inputZcashAddress.onEditorActionDone(::onSubmit).also { tapped(SEND_DONE_ADDRESS) }
        binding.inputZcashAmount.onEditorActionDone(::onSubmit).also { tapped(SEND_DONE_AMOUNT) }

        binding.inputZcashAddress.apply {
            doAfterTextChanged {
                val trim = text.toString().trim()
                if (text.toString() != trim) {
                    binding.inputZcashAddress
                        .findViewById<EditText>(R.id.input_zcash_address).setText(trim)
                }
                onAddressChanged(trim)
            }
        }

        binding.inputMemo.doAfterTextChanged {
            sendViewModel.memo = binding.inputMemo.text.toString()
            updateMemoCount()
        }

        binding.textLayoutAddress.setEndIconOnClickListener {
            mainActivity?.maybeOpenScan().also { tapped(SEND_SCAN) }
        }

        // new behaviors
        binding.boxShieldedFunds.isClickable = true
        binding.boxTransparentFunds.isClickable = true
        useShieldedFunds = true
        binding.boxShieldedFunds.setOnClickListener {
            if (!it.isSelected) {
                useShieldedFunds = true
            }
        }
        binding.boxTransparentFunds.setOnClickListener {
            if (!it.isSelected) {
                useShieldedFunds = false
            }
        }
        binding.textSubheader.text.toColoredSpan(R.color.colorPrimaryVariant, "shielded").let { spannable ->
            val start = binding.textSubheader.text.indexOf("transparent")
            spannable.setSpan(ForegroundColorSpan(R.color.colorSecondaryVariant.toAppColor()), start, start + "transparent".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.textSubheader.text = spannable
        }

        // memo
        sendViewModel.afterInitFromAddress {
            binding.textIncludedAddress.text = "sent from ${sendViewModel.fromAddress}"
        }

        binding.buttonAddMemo.setOnClickListener {
            onAddMemo((maxZatoshi ?: 0L) > 0L)
        }

        binding.clearMemo.setOnClickListener {
            onClearMemo().also { tapped(SEND_MEMO_CLEAR) }
        }

        binding.checkIncludeAddress.setOnCheckedChangeListener { _, _->
            onIncludeAddressInMemo(binding.checkIncludeAddress.isChecked)
        }
    }

    private fun selectGroup(group: ViewGroup, isSelected: Boolean) {
        group.isSelected = isSelected
        group.forEach {
            it.isSelected = isSelected
        }
    }

    private fun onAddressChanged(address: String) {
        if (address.length <= pasteLimit) {
            updateClipboardBanner()
        } else {
            sendViewModel.toAddress = binding.inputZcashAddress.text.toString()
            resumedScope.launch {
                var type = when (sendViewModel.validateAddress(address)) {
                    is Synchronizer.AddressType.Transparent -> "This is a valid transparent address" to R.color.zcashGreen
                    is Synchronizer.AddressType.Shielded -> "This is a valid shielded address" to R.color.zcashGreen
                    is Synchronizer.AddressType.Invalid -> "This address appears to be invalid" to R.color.zcashRed
                }
                if (address == sendViewModel.synchronizer.getAddress()) type =
                    "Warning, this appears to be your address!" to R.color.zcashRed
                binding.textLayoutAddress.helperText = type.first
                binding.textLayoutAddress.setHelperTextColor(ColorStateList.valueOf(type.second.toAppColor()))
            }
        }
    }


    private fun onSubmit(unused: EditText? = null) {
        // TODO: tech debt: improve this logic to be driven by the model rather than this clumsy storage of data in the UI
        sendViewModel.toAddress = binding.inputZcashAddress.text.toString()
        binding.inputZcashAmount.convertZecToZatoshi()?.let { sendViewModel.zatoshiAmount = it }
        sendViewModel.memo = if (useShieldedFunds) binding.inputMemo.text.toString() else ""
        sendViewModel.validate(maxZatoshi).onFirstWith(resumedScope) {
            if (it == null) {
                sendViewModel.funnel(Send.SendPageComplete)
                mainActivity?.safeNavigate(R.id.action_nav_send_to_send_confirm)
            } else {
                resumedScope.launch {
                    binding.textAddressError.text = it
                    delay(3000L)
                    binding.textAddressError.text = ""
                }
            }
        }
    }

    private fun onMax() {
        if (maxZatoshi != null) {
            binding.inputZcashAmount.apply {
                setText(maxZatoshi.convertZatoshiToZecString(8))
                postDelayed({
                    requestFocus()
                    setSelection(text?.length ?: 0)
                }, 10L)
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
        applyModel()
        updateClipboardBanner()
        sendViewModel.synchronizer.balances.collectWith(resumedScope) {
            onBalanceUpdated(it)
        }
    }

    private fun applyModel() {
        useShieldedFunds = sendViewModel.useShieldedFunds
        if (sendViewModel.zatoshiAmount > 0L) {
            sendViewModel.zatoshiAmount.convertZatoshiToZecString(8).let { amount ->
                binding.inputZcashAmount.setText(amount)
            }
        } else {
            binding.inputZcashAmount.setText(null)
        }
        binding.inputZcashAddress.setText(sendViewModel.toAddress)
        applyMemo()
    }

    fun applyMemo() {
        if (sendViewModel.isMemoAdded) {
            binding.groupMemo.visibility = View.VISIBLE
            binding.buttonAddMemo.visibility = View.GONE
        } else {
            binding.groupMemo.visibility = View.GONE
            binding.buttonAddMemo.visibility = View.VISIBLE
        }
        selectGroup(binding.boxShieldedFunds, sendViewModel.useShieldedFunds)
        selectGroup(binding.boxTransparentFunds, !sendViewModel.useShieldedFunds)

        binding.inputMemo.setText(sendViewModel.memo)
        binding.checkIncludeAddress.isChecked = sendViewModel.includeFromAddress
        binding.textIncludedAddress.goneIf(!sendViewModel.useShieldedFunds || !sendViewModel.includeFromAddress || !sendViewModel.isMemoAdded)
    }

    private fun onBalanceUpdated(balance: WalletBalance) {
        val zecString= balance.availableZatoshi.coerceAtLeast(0L).convertZatoshiToZecString(8)
        binding.textLayoutAmount.helperText =
            "You have $zecString available"
        binding.textShieldedFundsAmount.text = "\$${zecString}"
        maxZatoshi = (balance.availableZatoshi - ZcashSdk.MINERS_FEE_ZATOSHI).coerceAtLeast(0L)
    }

    override fun onPrimaryClipChanged() {
        twig("clipboard changed!")
        updateClipboardBanner()
    }

    private fun updateClipboardBanner() {
        val invalidAddressOnClipboard = loadAddressFromClipboard() == null
        val hidePaste = invalidAddressOnClipboard || (binding.inputZcashAddress.text ?: "").length >= pasteLimit - 1
        binding.textPaste.goneIf(hidePaste)
        binding.textLayoutAddress.helperText =
            "${if (hidePaste) "E" else "Paste or e"}nter a valid Zcash address"
        binding.textLayoutAddress.setHelperTextColor(ColorStateList.valueOf(R.color.text_light_dimmed.toAppColor()))
    }

    private fun onAddMemo(hasShieldedFunds: Boolean) {
        if (!useShieldedFunds) {
            //TODO: track dialog reference
            val builder = MaterialAlertDialogBuilder(mainActivity)
                .setTitle("Shielded Transaction Required")
                .setMessage("Memos are not supported for transparent transactions. To add a memo, you must send shielded funds.")
                .setCancelable(true)
                .setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
            if (hasShieldedFunds) {
                builder.setNegativeButton("Switch to Shielded") { dialog, _ ->
                    dialog.dismiss()
                    useShieldedFunds = true
                    onAddMemo(hasShieldedFunds)
                }
            }
            builder.show()
        } else {
            sendViewModel.isMemoAdded = true
            applyMemo()
            binding.inputMemo.requestFocus()
        }
    }

    private fun onClearMemo() {
        sendViewModel.isMemoAdded = false
        binding.inputMemo.setText("")
        sendViewModel.memo = ""
        binding.groupMemo.visibility = View.GONE
        binding.buttonAddMemo.visibility = View.VISIBLE
        binding.textIncludedAddress.visibility = View.GONE
    }

    private fun resetMemo(): Boolean {
        if (sendViewModel.memo.isNullOrEmpty()) {
            onClearMemo()
            return true
        } else {
            MaterialAlertDialogBuilder(mainActivity)
                .setTitle("Are you sure?")
                .setMessage("Memos are not supported for transparent transactions. If you switch to transparent funds, your memo will be lost.")
                .setCancelable(true)
                .setPositiveButton("Clear Memo") { dialog, _ ->
                    dialog.dismiss()
                    onClearMemo()
                    useShieldedFunds = false
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            return false
        }
    }

    private fun onIncludeAddressInMemo(checked: Boolean) {
        binding.textIncludedAddress.goneIf(!checked)
        sendViewModel.includeFromAddress = checked
        if (checked) {
            tapped(SEND_MEMO_INCLUDE)
//            getString(R.string.send_memo_included_message)
        } else {
            tapped(SEND_MEMO_EXCLUDE)
//            getString(R.string.send_memo_excluded_message)
        }
        updateMemoCount()
    }

    private fun updateMemoCount() {
        var count = sendViewModel.createMemoToSend().length
        binding.textMemoCount.text = "$count/512"
        val color = if (count > 512) R.color.zcashRed else R.color.text_light_dimmed
        binding.textMemoCount.setTextColor(color.toAppColor())
    }

    private fun onPaste() {
        mainActivity?.clipboard?.let { clipboard ->
            if (clipboard.hasPrimaryClip()) {
                binding.inputZcashAddress.setText(clipboard.text())
            }
        }
        binding.textPaste.visibility = View.GONE
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