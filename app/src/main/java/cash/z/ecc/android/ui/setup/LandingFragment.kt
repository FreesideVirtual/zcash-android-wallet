package cash.z.ecc.android.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import cash.z.ecc.android.R
import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.databinding.FragmentLandingBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.feedback.MetricType.SEED_CREATION
import cash.z.ecc.android.feedback.measure
import cash.z.ecc.android.isEmulator
import cash.z.ecc.android.lockbox.LockBox
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.LockBoxKey
import cash.z.ecc.kotlin.mnemonic.Mnemonics
import dagger.Module
import dagger.android.ContributesAndroidInjector
import javax.inject.Inject

class LandingFragment : BaseFragment<FragmentLandingBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val walletSetup: WalletSetupViewModel by activityViewModels { viewModelFactory }
    private var skipCount: Int = 0

    override fun inflate(inflater: LayoutInflater): FragmentLandingBinding =
        FragmentLandingBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonPositive.setOnClickListener {
            when (binding.buttonPositive.text.toString().toLowerCase()) {
                "new" -> onNewWallet()
                "backup" -> onBackupWallet()
            }
        }
        binding.buttonNegative.setOnClickListener {
            when (binding.buttonNegative.text.toString().toLowerCase()) {
                "restore" -> onRestoreWallet()
                else -> onSkip(++skipCount)
            }
        }
    }

    private fun onSkip(count: Int) {
        when (count) {
            1 -> {
                binding.textMessage.text =
                    "Are you sure? Without a backup, funds can be lost FOREVER!"
                binding.buttonNegative.text = "Later"
            }
            2 -> {
                binding.textMessage.text =
                    "You can't backup later. You're probably going to lose your funds!"
                binding.buttonNegative.text = "I've been warned"
            }
            else -> {
                onEnterWallet()
            }
        }
    }

    private fun onRestoreWallet() {
        if (ZcashWalletApp.instance.isEmulator()) {
            onEnterWallet()
        } else {
            Toast.makeText(activity, "Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onNewWallet() {
        mainActivity?.feedback?.measure(SEED_CREATION) {
            walletSetup.createSeed()
        }

        binding.textMessage.text = "Wallet created! Congratulations!"
        binding.buttonNegative.text = "Skip"
        binding.buttonPositive.text = "Backup"
        mainActivity?.playSound("sound_receive_small.mp3")
        mainActivity?.vibrateSuccess()
    }

    private fun onBackupWallet() {
        skipCount = 0
        mainActivity?.navController?.navigate(R.id.action_nav_landing_to_nav_backup)
    }

    private fun onEnterWallet() {
        skipCount = 0
        mainActivity?.navController?.popBackStack()
    }
}

@Module
abstract class LandingFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): LandingFragment
}