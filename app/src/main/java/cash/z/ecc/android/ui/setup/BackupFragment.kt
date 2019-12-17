package cash.z.ecc.android.ui.setup

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cash.z.ecc.android.R
import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.databinding.FragmentBackupBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.lockbox.LockBox
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.LockBoxKey
import cash.z.ecc.android.ui.util.AddressPartNumberSpan
import cash.z.ecc.kotlin.mnemonic.Mnemonics
import dagger.Module
import dagger.android.ContributesAndroidInjector

class BackupFragment : BaseFragment<FragmentBackupBinding>() {

    override fun inflate(inflater: LayoutInflater): FragmentBackupBinding =
        FragmentBackupBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            applySpan(
                textAddressPart1, textAddressPart2, textAddressPart3,
                textAddressPart4, textAddressPart5, textAddressPart6,
                textAddressPart7, textAddressPart8, textAddressPart9,
                textAddressPart10, textAddressPart11, textAddressPart12,
                textAddressPart13, textAddressPart14, textAddressPart15,
                textAddressPart16, textAddressPart17, textAddressPart18,
                textAddressPart19, textAddressPart20, textAddressPart21,
                textAddressPart22, textAddressPart23, textAddressPart24
            )
        }
        binding.buttonPositive.setOnClickListener {
            onEnterWallet()
        }
    }

    private fun onEnterWallet() {
        Toast.makeText(activity, "Backup verification coming soon! For now, enjoy your new wallet!", Toast.LENGTH_LONG).show()
        mainActivity?.navController?.popBackStack(R.id.wallet_setup_navigation, true)
    }

    private fun applySpan(vararg textViews: TextView) {
        val words = loadSeedWords()
        val thinSpace = "\u2005" // 0.25 em space
        textViews.forEachIndexed { index, textView ->
            val numLength = "$index".length
            val word = words[index]
            // TODO: work with a charsequence here, rather than constructing a String
            textView.text = SpannableString("${index + 1}$thinSpace${String(word)}").apply {
                setSpan(AddressPartNumberSpan(), 0, 1 + numLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun loadSeedWords(): List<CharArray> {
        val lockBox = LockBox(ZcashWalletApp.instance)
        val mnemonics = Mnemonics()
        val seed = lockBox.getBytes(LockBoxKey.SEED)!!
        return mnemonics.nextMnemonicList(seed)
    }
}


@Module
abstract class BackupFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): BackupFragment
}