package cash.z.ecc.android.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendConfirmBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.ext.onClickNavBack
import cash.z.ecc.android.ui.MainActivity
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.ext.abbreviatedAddress
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.launch

class SendConfirmFragment : BaseFragment<FragmentSendConfirmBinding>() {
    override fun inflate(inflater: LayoutInflater): FragmentSendConfirmBinding =
        FragmentSendConfirmBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity?.apply {
            binding.buttonNext.setOnClickListener {
                onSend()
            }
            binding.backButtonHitArea.onClickNavBack()
            mainActivity?.lifecycleScope?.launch {
                binding.textConfirmation.text =
                    "Send ${sendViewModel?.zatoshiAmount.convertZatoshiToZecString(8)} ZEC to ${sendViewModel?.toAddress.abbreviatedAddress()}?"
            }
            sendViewModel.memo.trim().isNotEmpty().let { hasMemo ->
                binding.radioIncludeAddress.isChecked = hasMemo
                binding.radioIncludeAddress.goneIf(!hasMemo)
            }
        }
    }

    private fun onSend() {
        mainActivity?.navController?.navigate(R.id.action_nav_send_confirm_to_send_final)
    }
}


@Module
abstract class SendConfirmFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): SendConfirmFragment
}
