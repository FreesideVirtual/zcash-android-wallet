package cash.z.ecc.android.ext

import android.view.View
import android.view.View.*
import cash.z.ecc.android.ui.MainActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow

fun View.goneIf(isGone: Boolean) {
    visibility = if (isGone) GONE else VISIBLE
}

fun View.invisibleIf(isInvisible: Boolean) {
    visibility = if (isInvisible) INVISIBLE else VISIBLE
}

fun View.disabledIf(isDisabled: Boolean) {
    isEnabled = !isDisabled
}

fun View.onClickNavTo(navResId: Int) {
    setOnClickListener {
        (context as? MainActivity)?.navController?.navigate(navResId)
            ?: throw IllegalStateException("Cannot navigate from this activity. " +
                    "Expected MainActivity but found ${context.javaClass.simpleName}")
    }
}

fun View.onClickNavUp() {
    setOnClickListener {
        (context as? MainActivity)?.navController?.navigateUp()
            ?: throw IllegalStateException(
                "Cannot navigate from this activity. " +
                        "Expected MainActivity but found ${context.javaClass.simpleName}"
            )
    }
}

fun View.onClickNavBack() {
    setOnClickListener {
        (context as? MainActivity)?.navController?.popBackStack()
            ?: throw IllegalStateException(
                "Cannot navigate from this activity. " +
                        "Expected MainActivity but found ${context.javaClass.simpleName}"
            )
    }
}

fun View.clicks() = channelFlow<View> {
    setOnClickListener {
        offer(this@clicks)
    }
    awaitClose {
        setOnClickListener(null)
    }
}