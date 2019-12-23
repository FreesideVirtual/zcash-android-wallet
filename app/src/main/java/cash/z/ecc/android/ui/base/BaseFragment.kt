package cash.z.ecc.android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import cash.z.ecc.android.ui.MainActivity
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.coroutineContext

abstract class BaseFragment<T : ViewBinding> : DaggerFragment() {
    val mainActivity: MainActivity? get() = activity as MainActivity?

    lateinit var binding: T

    lateinit var resumedScope: CoroutineScope

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        resumedScope = lifecycleScope.coroutineContext.let {
            CoroutineScope(it + SupervisorJob(it[Job]))
        }
    }

    override fun onPause() {
        super.onPause()
        resumedScope.cancel()
    }

    // inflate is static in the ViewBinding class so we can't handle this ourselves
    // each fragment must call FragmentMyLayoutBinding.inflate(inflater)
    abstract fun inflate(@NonNull inflater: LayoutInflater): T
}