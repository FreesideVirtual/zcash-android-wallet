package cash.z.ecc.android.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import cash.z.ecc.android.BuildConfig
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentProfileBinding
import cash.z.ecc.android.di.viewmodel.viewModel
import cash.z.ecc.android.ext.onClick
import cash.z.ecc.android.ext.onClickNavBack
import cash.z.ecc.android.ext.onClickNavTo
import cash.z.ecc.android.feedback.FeedbackFile
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.wallet.sdk.ext.toAbbreviatedAddress
import kotlinx.coroutines.launch
import okio.Okio

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModel()

    override fun inflate(inflater: LayoutInflater): FragmentProfileBinding =
        FragmentProfileBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.hitAreaClose.onClickNavBack()
        binding.buttonBackup.onClickNavTo(R.id.action_nav_profile_to_nav_backup)
        binding.textVersion.text = BuildConfig.VERSION_NAME
        onClick(binding.buttonLogs) {
            onViewLogs()
        }
        onClick(binding.buttonFeedback) {
            onSendFeedback()
        }
    }

    override fun onResume() {
        super.onResume()
        resumedScope.launch {
            binding.textAddress.text = viewModel.getAddress().toAbbreviatedAddress(12, 12)
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

    private fun onSendFeedback() {
        mainActivity?.showSnackbar("Feedback feature coming soon!")
    }

    private fun loadLogFileAsText(): String? {
        val feedbackFile: FeedbackFile =
            mainActivity?.feedbackCoordinator?.findObserver() ?: return null
        Okio.buffer(Okio.source(feedbackFile.file)).use {
            return it.readUtf8()
        }
    }
}