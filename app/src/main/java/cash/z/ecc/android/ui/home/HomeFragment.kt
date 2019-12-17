package cash.z.ecc.android.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentHomeBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.ext.onClickNavTo
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.home.HomeFragment.BannerAction.*
import cash.z.ecc.android.ui.setup.WalletSetupViewModel
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.WalletSetupState.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private lateinit var numberPad: List<TextView>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val walletSetup: WalletSetupViewModel by activityViewModels { viewModelFactory }

    override fun inflate(inflater: LayoutInflater): FragmentHomeBinding =
        FragmentHomeBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            numberPad = arrayListOf(
                buttonNumberPad0,
                buttonNumberPad1,
                buttonNumberPad2,
                buttonNumberPad3,
                buttonNumberPad4,
                buttonNumberPad5,
                buttonNumberPad6,
                buttonNumberPad7,
                buttonNumberPad8,
                buttonNumberPad9,
                buttonNumberPadDecimal,
                buttonNumberPadBack
            )
            hitAreaReceive.onClickNavTo(R.id.action_nav_home_to_nav_receive)
            iconDetail.onClickNavTo(R.id.action_nav_home_to_nav_detail)
            textDetail.onClickNavTo(R.id.action_nav_home_to_nav_detail)
            hitAreaScan.onClickNavTo(R.id.action_nav_home_to_nav_send)

            textBannerAction.setOnClickListener {
                onBannerAction(BannerAction.from((it as? TextView)?.text?.toString()))
            }
        }

        // TODO: trigger this from presenter
        onNoFunds()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        walletSetup.checkSeed().onEach {
            when(it) {
                NO_SEED -> {
                    mainActivity?.navController?.navigate(R.id.action_nav_home_to_create_wallet)
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun onBannerAction(action: BannerAction) {
        when (action) {
            LEARN_MORE -> {
                MaterialAlertDialogBuilder(activity)
                    .setMessage("To make full use of this wallet, deposit funds to your address or tap the faucet to trigger a tiny automatic deposit.\n\nFaucet funds are made available for the community by the community for testing. So please be kind enough to return what you borrow!")
                    .setTitle("No Balance")
                    .setCancelable(true)
                    .setPositiveButton("Tap Faucet") { dialog, _ ->
                        dialog.dismiss()
                        setBanner("Tapping faucet...", CANCEL)
                    }
                    .setNegativeButton("View Address") { dialog, _ ->
                        dialog.dismiss()
                        mainActivity?.navController?.navigate(R.id.action_nav_home_to_nav_receive)
                    }
                    .show()
            }
            CANCEL -> {
                // TODO: trigger banner / balance update
                onNoFunds()
            }
        }
    }

    private fun onNoFunds() {
        setBanner("No Balance", LEARN_MORE)
    }

    private fun setBanner(message: String = "", action: BannerAction = CLEAR) {
        with(binding) {
            val hasMessage = !message.isEmpty() || action != CLEAR
            groupBalance.goneIf(hasMessage)
            groupBanner.goneIf(!hasMessage)
            layerLock.goneIf(!hasMessage)

            textBannerMessage.text = message
            textBannerAction.text = action.action
        }
    }

    enum class BannerAction(val action: String) {
        LEARN_MORE("Learn More"),
        CANCEL("Cancel"),
        CLEAR("");

        companion object {
            fun from(action: String?): BannerAction {
                values().forEach {
                    if (it.action == action) return it
                }
                throw IllegalArgumentException("Invalid BannerAction: $action")
            }
        }
    }
}


@Module
abstract class HomeFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): HomeFragment
}