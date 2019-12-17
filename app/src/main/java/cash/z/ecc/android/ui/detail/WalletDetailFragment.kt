package cash.z.ecc.android.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import cash.z.ecc.android.databinding.FragmentDetailBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ext.onClickNavUp
import cash.z.ecc.android.ui.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

class WalletDetailFragment : BaseFragment<FragmentDetailBinding>() {
    override fun inflate(inflater: LayoutInflater): FragmentDetailBinding =
        FragmentDetailBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButtonHitArea.onClickNavUp()
    }
}


@Module
abstract class WalletDetailFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): WalletDetailFragment
}