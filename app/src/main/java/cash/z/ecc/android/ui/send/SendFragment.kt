package cash.z.ecc.android.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import cash.z.ecc.android.databinding.FragmentSendBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ext.onClickNavUp
import cash.z.ecc.android.ui.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

class SendFragment : BaseFragment<FragmentSendBinding>() {
    override fun inflate(inflater: LayoutInflater): FragmentSendBinding =
        FragmentSendBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButtonHitArea.onClickNavUp()
    }
}


@Module
abstract class SendFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): SendFragment
}
