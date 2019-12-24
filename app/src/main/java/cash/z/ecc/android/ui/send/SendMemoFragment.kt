package cash.z.ecc.android.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendMemoBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ext.onClickNavBack
import cash.z.ecc.android.ext.onClickNavUp
import cash.z.ecc.android.ui.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

class SendMemoFragment : BaseFragment<FragmentSendMemoBinding>() {
    override fun inflate(inflater: LayoutInflater): FragmentSendMemoBinding =
        FragmentSendMemoBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNext.setOnClickListener {
            onAddMemo()
        }
        binding.buttonSkip.setOnClickListener {
            binding.inputMemo.setText("")
            mainActivity?.sendViewModel?.memo = ""
            mainActivity?.navController?.navigate(R.id.action_nav_send_memo_to_send_confirm)
        }
        binding.backButtonHitArea.onClickNavBack()
        binding.radioIncludeAddress.setOnClickListener {
            if (binding.radioIncludeAddress.isActivated) {
                binding.radioIncludeAddress.isChecked = false
            } else {
                binding.radioIncludeAddress.isActivated = true
            }
        }
        binding.inputMemo.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onAddMemo()
                true
            } else {
                false
            }
        }
        binding.radioIncludeAddress.requestFocus()
    }

    private fun onAddMemo() {
        mainActivity?.sendViewModel?.memo = binding.inputMemo.text.toString()
        mainActivity?.navController?.navigate(R.id.action_nav_send_memo_to_send_confirm)
    }
}


@Module
abstract class SendMemoFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): SendMemoFragment
}