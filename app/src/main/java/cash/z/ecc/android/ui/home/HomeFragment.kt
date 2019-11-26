package cash.z.ecc.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentHomeBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ui.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun inflate(inflater: LayoutInflater): FragmentHomeBinding =
        FragmentHomeBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.hitAreaReceive.setOnClickListener {
            mainActivity?.navController?.navigate(R.id.action_nav_home_to_nav_receive)
        }
        binding.iconDetail.setOnClickListener {
            mainActivity?.navController?.navigate(R.id.action_nav_home_to_nav_detail)
        }
        binding.hitAreaScan.setOnClickListener {
            mainActivity?.navController?.navigate(R.id.action_nav_home_to_nav_send)
        }
    }
}


@Module
abstract class HomeFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment
}