package cash.z.ecc.android.ext

import android.view.View
import android.view.View.*
import cash.z.ecc.android.ui.MainActivity

fun View.goneIf(isGone: Boolean) {
    visibility = if (isGone) GONE else VISIBLE
}

fun View.invisibleIf(isInvisible: Boolean) {
    visibility = if (isInvisible) INVISIBLE else VISIBLE
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