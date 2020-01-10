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
            onTopButton()
        }
        binding.buttonSkip.setOnClickListener {
            onBottomButton()
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
                onTopButton()
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
        sendViewModel.isShielded.let { isShielded ->
            binding.groupShielded.goneIf(!isShielded)
            binding.groupTransparent.goneIf(isShielded)
            if (isShielded) {
                binding.inputMemo.setText(sendViewModel.memo)
                binding.checkIncludeAddress.isChecked = sendViewModel.includeFromAddress
                binding.buttonNext.text = "ADD MEMO"
                binding.buttonSkip.text = "SEND WITHOUT MEMO"
            } else {
                binding.buttonNext.text = "GO BACK"
                binding.buttonSkip.text = "PROCEED"
            }
        }
    }

    private fun onIncludeMemo(checked: Boolean) {
        binding.textIncludedAddress.goneIf(!checked)
        sendViewModel.includeFromAddress = checked
        if (checked) binding.inputMemo.setHint("") else binding.inputMemo.setHint("Add a memo here")
    }

    private fun onTopButton() {
        if (sendViewModel.isShielded) {
            sendViewModel.memo = binding.inputMemo.text.toString()
            onNext()
        } else {
            mainActivity?.navController?.navigate(R.id.action_nav_send_memo_to_nav_send_address)
        }
    }

    private fun onBottomButton() {
        binding.inputMemo.setText("")
        sendViewModel.memo = ""
        sendViewModel.includeFromAddress = false
        onNext()
    }

    private fun onNext() {
        mainActivity?.navController?.navigate(R.id.action_nav_send_memo_to_send_confirm)
    }
}