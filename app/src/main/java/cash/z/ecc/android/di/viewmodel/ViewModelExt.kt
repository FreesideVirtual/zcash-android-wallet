package cash.z.ecc.android.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cash.z.ecc.android.ui.MainActivity
import cash.z.ecc.android.ui.base.BaseFragment


inline fun <reified VM : ViewModel> MainActivity.viewModel() = object : Lazy<VM> {
    val cached: VM? = null
    override fun isInitialized(): Boolean = cached != null
    override val value: VM get() {
        return cached ?: component.viewModels()[VM::class.java]
    }
}

inline fun <reified VM : ViewModel> BaseFragment<*>.viewModel() = object : Lazy<VM> {
    val cached: VM? = null
    override fun isInitialized(): Boolean = cached != null
    override val value: VM get() {
        return if (cached != null) cached else {
            val factory =  mainActivity?.component?.viewModelFactory()
                ?: throw IllegalStateException("Error: mainActivity should not be null by the time the ${VM::class.java.simpleName} viewmodel is lazily accessed!")
            ViewModelProvider(this@viewModel, factory)[VM::class.java]
        }
    }
}

inline fun <reified VM : ViewModel> BaseFragment<*>.activityViewModel() = object : Lazy<VM> {
    val cached: VM? = null
    override fun isInitialized(): Boolean = cached != null
    override val value: VM get() {
        return cached ?: mainActivity?.component?.viewModels()?.get(VM::class.java)
        ?: throw IllegalStateException("Error: mainActivity should not be null by the time the ${VM::class.java.simpleName} activityViewModel is lazily accessed!")
    }
}