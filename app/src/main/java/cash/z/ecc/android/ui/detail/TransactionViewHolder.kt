package cash.z.ecc.android.ui.detail

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cash.z.ecc.android.R
import cash.z.ecc.android.ext.goneIf
import cash.z.ecc.android.ext.toAppColor
import cash.z.wallet.sdk.entity.ConfirmedTransaction
import cash.z.wallet.sdk.ext.toAbbreviatedAddress
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.isShielded
import java.text.SimpleDateFormat
import java.util.*

class TransactionViewHolder<T : ConfirmedTransaction>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val indicator = itemView.findViewById<View>(R.id.indicator)
    private val amountText = itemView.findViewById<TextView>(R.id.text_transaction_amount)
    private val topText = itemView.findViewById<TextView>(R.id.text_transaction_top)
    private val bottomText = itemView.findViewById<TextView>(R.id.text_transaction_bottom)
    private val shieldIcon = itemView.findViewById<View>(R.id.image_shield)
    private val formatter = SimpleDateFormat("M/d h:mma", Locale.getDefault())

    fun bindTo(transaction: T?) {

        // update view
        var lineOne: String = ""
        var lineTwo: String = ""
        var amount: String = ""
        var amountColor: Int = 0
        var indicatorBackground: Int = 0
        
        transaction?.apply {
            amount = value.convertZatoshiToZecString()
            // TODO: these might be good extension functions
            val timestamp = formatter.format(blockTimeInSeconds * 1000L)
            val isMined = blockTimeInSeconds != 0L
            when {
                !toAddress.isNullOrEmpty() -> {
                    lineOne = "You paid ${toAddress?.toAbbreviatedAddress()}"
                    lineTwo = if (isMined) "Sent $timestamp" else "Pending confirmation"
                    amount = "- $amount"
                    amountColor = R.color.zcashRed
                    indicatorBackground = R.drawable.background_indicator_outbound
                }
                raw == null || raw?.isEmpty() == true -> {
                    lineOne = "Unknown paid you"
                    lineTwo = "Received $timestamp"
                    amount = "+ $amount"
                    amountColor = R.color.zcashGreen
                    indicatorBackground = R.drawable.background_indicator_inbound
                }
                else -> {
                    lineOne = "Unknown"
                    lineTwo = "Unknown"
                }
            }
        }

        topText.text = lineOne
        bottomText.text = lineTwo
        amountText.text = amount
        amountText.setTextColor(amountColor.toAppColor())
        val context = itemView.context
        indicator.background = context.resources.getDrawable(indicatorBackground)
        shieldIcon.goneIf(!transaction?.toAddress.isShielded())
    }
}