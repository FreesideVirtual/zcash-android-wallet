package cash.z.ecc.android.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentDetailBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ext.onClick
import cash.z.ecc.android.ext.onClickNavUp
import cash.z.ecc.android.feedback.FeedbackFile
import cash.z.ecc.android.ui.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector
import okio.Okio


class WalletDetailFragment : BaseFragment<FragmentDetailBinding>() {

    override fun inflate(inflater: LayoutInflater): FragmentDetailBinding =
        FragmentDetailBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButtonHitArea.onClickNavUp()

        onClick(binding.buttonFeedback) {
            onSendFeedback()
        }
        onClick(binding.buttonLogs) {
            onViewLogs()
        }
        onClick(binding.buttonBackup, 1L) {
            onBackupWallet()
        }
    }

    private fun onSendFeedback() {
        mainActivity?.showSnackbar("Feedback not yet implemented.")
    }

    }

    private fun onViewLogs() {
        loadLogFileAsText().let { logText ->
            if (logText == null) {
                mainActivity?.showSnackbar("Log file not found!")
            } else {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, logText)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share Log File")
                startActivity(shareIntent)
            }
        }
    }

    private fun onBackupWallet() {
        mainActivity?.navController?.navigate(R.id.action_nav_detail_to_backup_wallet)
    }

    private fun loadLogFileAsText(): String? {
        val feedbackFile: FeedbackFile =
            mainActivity?.feedbackCoordinator?.findObserver() ?: return null
        Okio.buffer(Okio.source(feedbackFile.file)).use {
            return it.readUtf8()
        }
    }
}


@Module
abstract class WalletDetailFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): WalletDetailFragment
}