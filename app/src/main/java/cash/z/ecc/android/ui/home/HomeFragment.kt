package cash.z.ecc.android.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentHomeBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.di.viewmodel.viewModel
import cash.z.ecc.android.ext.disabledIf
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.ext.onClickNavTo
import cash.z.ecc.android.ext.toColoredSpan
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.home.HomeFragment.BannerAction.*
import cash.z.ecc.android.ui.send.SendViewModel
import cash.z.ecc.android.ui.setup.WalletSetupViewModel
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.WalletSetupState.NO_SEED
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.Synchronizer.Status.SYNCING
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.convertZecToZatoshi
import cash.z.wallet.sdk.ext.safelyConvertToBigDecimal
import cash.z.wallet.sdk.ext.twig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private lateinit var numberPad: List<TextView>
    private lateinit var uiModel: HomeViewModel.UiModel

    private val walletSetup: WalletSetupViewModel by activityViewModel(false)
    private val sendViewModel: SendViewModel by activityViewModel()
    private val viewModel: HomeViewModel by viewModel()

    override fun inflate(inflater: LayoutInflater): FragmentHomeBinding =
        FragmentHomeBinding.inflate(inflater)


    //
    // LifeCycle
    //

    override fun onAttach(context: Context) {
        twig("HomeFragment.onAttach")
        super.onAttach(context)

        // this will call startSync either now or later (after initializing with newly created seed)
        walletSetup.checkSeed().onEach {
            twig("Checking seed")
            if (it == NO_SEED) {
                // interact with user to create, backup and verify seed
                // leads to a call to startSync(), later (after accounts are created from seed)
                twig("Seed not found, therefore, launching seed creation flow")
                mainActivity?.navController?.navigate(R.id.action_nav_home_to_create_wallet)
            } else {
                twig("Found seed. Re-opening existing wallet")
                mainActivity?.startSync(walletSetup.openWallet())
            }
        }.launchIn(lifecycleScope)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        twig("HomeFragment.onViewCreated  uiModel: ${::uiModel.isInitialized}  saved: ${savedInstanceState != null}")
        with(binding) {
            numberPad = arrayListOf(
                buttonNumberPad0.asKey(),
                buttonNumberPad1.asKey(),
                buttonNumberPad2.asKey(),
                buttonNumberPad3.asKey(),
                buttonNumberPad4.asKey(),
                buttonNumberPad5.asKey(),
                buttonNumberPad6.asKey(),
                buttonNumberPad7.asKey(),
                buttonNumberPad8.asKey(),
                buttonNumberPad9.asKey(),
                buttonNumberPadDecimal.asKey(),
                buttonNumberPadBack.asKey()
            )
            hitAreaReceive.onClickNavTo(R.id.action_nav_home_to_nav_profile)
            iconDetail.onClickNavTo(R.id.action_nav_home_to_nav_detail)
            textDetail.onClickNavTo(R.id.action_nav_home_to_nav_detail)
            hitAreaScan.setOnClickListener {
                mainActivity?.maybeOpenScan()
            }

            textBannerAction.setOnClickListener {
                onBannerAction(BannerAction.from((it as? TextView)?.text?.toString()))
            }
            buttonSendAmount.setOnClickListener {
                onSend()
            }
            setSendAmount("0", false)
        }

        binding.buttonNumberPadBack.setOnLongClickListener {
            onClearAmount()
            true
        }

//        if (::uiModel.isInitialized) {
//            twig("uiModel exists!")
//            onModelUpdated(HomeViewModel.UiModel(), uiModel)
//        }
    }

    private fun onClearAmount() {
        if (::uiModel.isInitialized) {
            resumedScope.launch {
                binding.textSendAmount.text.apply {
                    while (uiModel.pendingSend != "0") {
                        viewModel.onChar('<')
                        delay(5)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        twig("HomeFragment.onResume  resumeScope.isActive: ${resumedScope.isActive}  $resumedScope")
        viewModel.initializeMaybe()
twig("onResume (A)")
        onClearAmount()
twig("onResume (B)")
        viewModel.uiModels.scanReduce { old, new ->
            onModelUpdated(old, new)
            new
        }.onCompletion {
            twig("uiModel.scanReduce completed.")
        }.catch { e ->
            twig("exception while processing uiModels $e")
            throw e
        }.launchIn(resumedScope)
twig("onResume (C)")

        // TODO: see if there is a better way to trigger a refresh of the uiModel on resume
        //       the latest one should just be in the viewmodel and we should just "resubscribe"
        //       but for some reason, this doesn't always happen, which kind of defeats the purpose
        //       of having a cold stream in the view model
        resumedScope.launch {
twig("onResume (pre-fresh)")
            viewModel.refreshBalance()
twig("onResume (post-fresh)")
        }
twig("onResume (D)")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        twig("HomeFragment.onSaveInstanceState")
        if (::uiModel.isInitialized) {
//            outState.putParcelable("uiModel", uiModel)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { inState ->
            twig("HomeFragment.onViewStateRestored")
//            onModelUpdated(HomeViewModel.UiModel(), inState.getParcelable("uiModel")!!)
        }
    }


    //
    // Public UI API
    //

    fun setSendEnabled(enabled: Boolean) {
        binding.buttonSendAmount.apply {
            isEnabled = enabled
//            backgroundTintList = ColorStateList.valueOf( resources.getColor( if(enabled) R.color.colorPrimary else R.color.zcashWhite_24) )
        }
    }

    fun setProgress(uiModel: HomeViewModel.UiModel) {
        if (!uiModel.processorInfo.hasData) {
            twig("Warning: ignoring progress update because the processor has not started.")
            return
        }

        val sendText = when {
            uiModel.isSynced -> "SEND AMOUNT"
            uiModel.status == Synchronizer.Status.DISCONNECTED -> "DISCONNECTED"
            uiModel.status == Synchronizer.Status.STOPPED -> "IDLE"
            uiModel.isDownloading -> "Downloading . . . ${uiModel.downloadProgress}%"
            uiModel.isValidating -> "Validating . . ."
            uiModel.isScanning -> "Scanning . . . ${uiModel.scanProgress}%"
            else -> "Updating"
        }
        binding.lottieButtonLoading.progress = if (uiModel.isSynced) 1.0f else uiModel.totalProgress * 0.82f // line fully closes at 82% mark
        binding.buttonSendAmount.text = sendText
        twig("Lottie progress set to ${binding.lottieButtonLoading.progress}  (isSynced? ${uiModel.isSynced})")
        twig("Send button set to: $sendText")

        val resId = if (uiModel.isSynced) R.color.selector_button_text_dark else R.color.selector_button_text_light
        binding.buttonSendAmount.setTextColor(resources.getColorStateList(resId))
    }

    /**
     * @param amount the amount to send represented as ZEC, without the dollar sign.
     */
    fun setSendAmount(amount: String, updateModel: Boolean = true) {
        binding.textSendAmount.text = "\$$amount".toColoredSpan(R.color.text_light_dimmed, "$")
        if (updateModel) {
            sendViewModel.zatoshiAmount = amount.safelyConvertToBigDecimal().convertZecToZatoshi()
        }
        binding.buttonSendAmount.disabledIf(amount == "0")
    }

    fun setAvailable(availableBalance: Long = -1L, totalBalance: Long = -1L) {
        val availableString = if (availableBalance < 0) "Updating" else availableBalance.convertZatoshiToZecString()
        binding.textBalanceAvailable.text = availableString
        binding.textBalanceDescription.apply {
            goneIf(availableBalance < 0)
            text = if (availableBalance != -1L && (availableBalance < totalBalance)) {
                val change = (totalBalance - availableBalance).convertZatoshiToZecString()
                "(expecting +$change ZEC)".toColoredSpan(R.color.text_light, "+$change")
            } else {
                "(enter an amount to send)"
            }
        }
    }

    fun setBanner(message: String = "", action: BannerAction = CLEAR) {
        with(binding) {
            val hasMessage = !message.isEmpty() || action != CLEAR
            groupBalance.goneIf(hasMessage)
            groupBanner.goneIf(!hasMessage)
            layerLock.goneIf(!hasMessage)

            textBannerMessage.text = message
            textBannerAction.text = action.action
        }
    }


    //
    // Private UI Events
    //

    private fun onModelUpdated(old: HomeViewModel.UiModel, new: HomeViewModel.UiModel) {
        twig("onModelUpdated: $new")
        uiModel = new
twig("onModelUpdated (A)")
        if (old.pendingSend != new.pendingSend) {
twig("onModelUpdated (B)")
            setSendAmount(new.pendingSend)
twig("onModelUpdated (C)")
        }
twig("onModelUpdated (D)")
        // TODO: handle stopped and disconnected flows
        setProgress(uiModel) // TODO: we may not need to separate anymore
twig("onModelUpdated (E)")
        if (new.status == SYNCING) onSyncing(new) else onSynced(new)
twig("onModelUpdated (F)")
        setSendEnabled(new.isSendEnabled)
twig("onModelUpdated (G) sendEnabled? ${new.isSendEnabled}")
        twig("DONE onModelUpdated")
    }

    private fun onSyncing(uiModel: HomeViewModel.UiModel) {
        setAvailable()
    }

    private fun onSynced(uiModel: HomeViewModel.UiModel) {
        binding.lottieButtonLoading.progress = 1.0f
        if (!uiModel.hasBalance) {
            onNoFunds()
        } else {
            setBanner("")
            setAvailable(uiModel.availableBalance, uiModel.totalBalance)
        }
    }

    private fun onSend() {
        mainActivity?.navController?.navigate(R.id.action_nav_home_to_send)
    }

    private fun onBannerAction(action: BannerAction) {
        when (action) {
            FUND_NOW -> {
                MaterialAlertDialogBuilder(activity)
                    .setMessage("To make full use of this wallet, deposit funds to your address.")
                    .setTitle("No Balance")
                    .setCancelable(true)
                    .setPositiveButton("View Address") { dialog, _ ->
                        dialog.dismiss()
                        mainActivity?.navController?.navigate(R.id.action_nav_home_to_nav_receive)
                    }
                    .show()
//                MaterialAlertDialogBuilder(activity)
//                    .setMessage("To make full use of this wallet, deposit funds to your address or tap the faucet to trigger a tiny automatic deposit.\n\nFaucet funds are made available for the community by the community for testing. So please be kind enough to return what you borrow!")
//                    .setTitle("No Balance")
//                    .setCancelable(true)
//                    .setPositiveButton("Tap Faucet") { dialog, _ ->
//                        dialog.dismiss()
//                        setBanner("Tapping faucet...", CANCEL)
//                    }
//                    .setNegativeButton("View Address") { dialog, _ ->
//                        dialog.dismiss()
//                        mainActivity?.navController?.navigate(R.id.action_nav_home_to_nav_receive)
//                    }
//                    .show()
            }
            CANCEL -> {
                // TODO: trigger banner / balance update
                onNoFunds()
            }
        }
    }

    private fun onNoFunds() {
        setBanner("No Balance", FUND_NOW)
    }


    //
    // Inner classes and extensions
    //

    enum class BannerAction(val action: String) {
        FUND_NOW("Fund Now"),
        CANCEL("Cancel"),
        NONE(""),
        CLEAR("clear");

        companion object {
            fun from(action: String?): BannerAction {
                values().forEach {
                    if (it.action == action) return it
                }
                throw IllegalArgumentException("Invalid BannerAction: $action")
            }
        }
    }

    private fun TextView.asKey(): TextView {
        val c = text[0]
        setOnClickListener {
            lifecycleScope.launch {
                twig("CHAR TYPED: $c")
                viewModel.onChar(c)
            }
        }
        return this
    }




    // TODO: remove these troubleshooting logs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        twig("HomeFragment.onCreate")
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        twig("HomeFragment.onActivityCreated")
    }
    override fun onStart() {
        super.onStart()
        twig("HomeFragment.onStart")
    }
    override fun onPause() {
        super.onPause()
        twig("HomeFragment.onPause  resumeScope.isActive: ${resumedScope.isActive}")
    }
    override fun onStop() {
        super.onStop()
        twig("HomeFragment.onStop")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        twig("HomeFragment.onDestroyView")
    }
    override fun onDestroy() {
        super.onDestroy()
        twig("HomeFragment.onDestroy")
    }
    override fun onDetach() {
        super.onDetach()
        twig("HomeFragment.onDetach")
    }
}