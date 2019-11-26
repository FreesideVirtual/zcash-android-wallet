package cash.z.ecc.android.ui.detail

import android.view.LayoutInflater
import cash.z.ecc.android.databinding.FragmentDetailBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ui.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

class WalletDetailFragment : BaseFragment<FragmentDetailBinding>() {
    override fun inflate(inflater: LayoutInflater): FragmentDetailBinding =
        FragmentDetailBinding.inflate(inflater)
}


@Module
abstract class WalletDetailFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeWalletDetailFragment(): WalletDetailFragment
}