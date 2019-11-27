package cash.z.ecc.android.ui.setup

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentBackupBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.util.AddressPartNumberSpan
import dagger.Module
import dagger.android.ContributesAndroidInjector

class BackupFragment : BaseFragment<FragmentBackupBinding>() {

    override fun inflate(inflater: LayoutInflater): FragmentBackupBinding =
        FragmentBackupBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            applySpan(
                textAddressPart1,
                textAddressPart2,
                textAddressPart3,
                textAddressPart4,
                textAddressPart5,
                textAddressPart6,
                textAddressPart7,
                textAddressPart8
            )
        }
        binding.buttonPositive.setOnClickListener {
            onEnterWallet()
        }
    }

    private fun onEnterWallet() {
        Toast.makeText(activity, "Backup verification coming soon! For now, enjoy your new wallet!", Toast.LENGTH_LONG).show()
        mainActivity?.navController?.navigate(R.id.action_nav_backup_to_nav_home)
    }

    private fun applySpan(vararg textViews: TextView) {
        val thinSpace = "\u2005" // 0.25 em space
        textViews.forEachIndexed { index, textView ->
            textView.text = SpannableString("${index + 1}$thinSpace${textView.text}").apply {
                setSpan(AddressPartNumberSpan(), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}


@Module
abstract class BackupFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): BackupFragment
}