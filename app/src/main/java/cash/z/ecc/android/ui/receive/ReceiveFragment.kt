package cash.z.ecc.android.ui.receive

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cash.z.android.qrecycler.QRecycler
import cash.z.ecc.android.databinding.FragmentReceiveBinding
import cash.z.ecc.android.di.annotation.FragmentScope
import cash.z.ecc.android.ext.onClickNavTo
import cash.z.ecc.android.ext.onClickNavUp
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.util.AddressPartNumberSpan
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.coroutines.launch

class ReceiveFragment : BaseFragment<FragmentReceiveBinding>() {
    override fun inflate(inflater: LayoutInflater): FragmentReceiveBinding =
        FragmentReceiveBinding.inflate(inflater)

    lateinit var qrecycler: QRecycler

    lateinit var addressParts: Array<TextView>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressParts = arrayOf(
            text_address_part_1,
            text_address_part_2,
            text_address_part_3,
            text_address_part_4,
            text_address_part_5,
            text_address_part_6,
            text_address_part_7,
            text_address_part_8
        )
        binding.backButton.onClickNavUp()
    }

    override fun onAttach(context: Context) {
        qrecycler = QRecycler() // inject! :)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            onAddressLoaded("zs1qduvdyuv83pyygjvc4cfcuc2wj5flnqn730iigf0tjct8k5ccs9y30p96j2gvn9gzyxm6q0vj12c4")
        }
    }

    private fun onAddressLoaded(address: String) {
        Log.e("TWIG", "onAddressLoaded:  $address length: ${address.length}")
        qrecycler.load(address)
            .withQuietZoneSize(3)
            .withCorrectionLevel(QRecycler.CorrectionLevel.MEDIUM)
            .into(receive_qr_code)

        address.chunked(address.length/8).forEachIndexed { i, part ->
            setAddressPart(i, part)
        }
    }

    private fun setAddressPart(index: Int, addressPart: String) {
        Log.e("TWIG", "setting address for part $index) $addressPart")
        val thinSpace = "\u2005" // 0.25 em space
        val textSpan = SpannableString("${index + 1}$thinSpace$addressPart")

        textSpan.setSpan(AddressPartNumberSpan(), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        addressParts[index].text = textSpan
    }
}


@Module
abstract class ReceiveFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFragment(): ReceiveFragment
}