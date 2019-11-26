package cash.z.ecc.android.ui.send

import android.view.LayoutInflater
import cash.z.ecc.android.databinding.FragmentSendBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ui.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

class SendFragment : BaseFragment<FragmentSendBinding>() {
    override fun inflate(inflater: LayoutInflater): FragmentSendBinding =
        FragmentSendBinding.inflate(inflater)
}


@Module
abstract class SendFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSendFragment(): SendFragment
}
