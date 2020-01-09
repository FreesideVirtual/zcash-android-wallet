package cash.z.ecc.android.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendMemoBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.ext.gone
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.ext.onClickNavTo
import cash.z.ecc.android.ui.base.BaseFragment

class SendMemoFragment : BaseFragment<FragmentSendMemoBinding>() {

    val sendViewModel: SendViewModel by activityViewModel()

    override fun inflate(inflater: LayoutInflater): FragmentSendMemoBinding =
        FragmentSendMemoBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNext.setOnClickListener {
            onAddMemo()
        }
        binding.buttonSkip.setOnClickListener {
            onSkip()
        }
        R.id.action_nav_send_memo_to_nav_send_address.let {
            binding.backButtonHitArea.onClickNavTo(it)
            onBackPressNavTo(it)
        }

        binding.checkIncludeAddress.setOnCheckedChangeListener { _, _->
            onIncludeMemo(binding.checkIncludeAddress.isChecked)
        }

        binding.inputMemo.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onAddMemo()
                true
            } else {
                false
            }
        }
        sendViewModel.afterInitFromAddress {
            binding.textIncludedAddress.text = "sent from ${sendViewModel.fromAddress}"
        }

        binding.textIncludedAddress.gone()

        applyModel()
    }

    private fun applyModel() {
        binding.inputMemo.setText(sendViewModel.memo)
        binding.checkIncludeAddress.isChecked = sendViewModel.includeFromAddress
    }

    private fun onIncludeMemo(checked: Boolean) {
        binding.textIncludedAddress.goneIf(!checked)
        sendViewModel.includeFromAddress = checked
        if (checked) binding.inputMemo.setHint("") else binding.inputMemo.setHint("Add a memo here")
    }

    private fun onSkip() {
        binding.inputMemo.setText("")
        sendViewModel.memo = ""
        sendViewModel.includeFromAddress = false
        onNext()
    }

    private fun onAddMemo() {
        sendViewModel.memo = binding.inputMemo.text.toString()
        onNext()
    }

    private fun onNext() {
        mainActivity?.navController?.navigate(R.id.action_nav_send_memo_to_send_confirm)
    }
}