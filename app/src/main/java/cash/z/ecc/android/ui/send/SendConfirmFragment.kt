package cash.z.ecc.android.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendConfirmBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.ext.onClickNavBack
import cash.z.ecc.android.ext.onClickNavTo
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.ext.toAbbreviatedAddress
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import kotlinx.coroutines.launch

class SendConfirmFragment : BaseFragment<FragmentSendConfirmBinding>() {

    val sendViewModel: SendViewModel by activityViewModel()

    override fun inflate(inflater: LayoutInflater): FragmentSendConfirmBinding =
        FragmentSendConfirmBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNext.setOnClickListener {
            onSend()
        }
        R.id.action_nav_send_confirm_to_nav_send_memo.let {
            binding.backButtonHitArea.onClickNavTo(it)
            onBackPressNavTo(it)
        }
        mainActivity?.lifecycleScope?.launch {
            binding.textConfirmation.text =
                "Send ${sendViewModel.zatoshiAmount.convertZatoshiToZecString(8)} ZEC to ${sendViewModel?.toAddress.toAbbreviatedAddress()}?"
        }
        sendViewModel.memo.trim().isNotEmpty().let { hasMemo ->
            binding.radioIncludeAddress.isChecked = hasMemo || sendViewModel.includeFromAddress
            binding.radioIncludeAddress.goneIf(!(hasMemo || sendViewModel.includeFromAddress))
        }
    }

    private fun onSend() {
        mainActivity?.navController?.navigate(R.id.action_nav_send_confirm_to_send_final)
    }
}