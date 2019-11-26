package cash.z.ecc.android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.viewbinding.ViewBinding
import cash.z.ecc.android.ui.MainActivity
import dagger.android.support.DaggerFragment

abstract class BaseFragment<T : ViewBinding> : DaggerFragment() {
    val mainActivity: MainActivity? get() = activity as MainActivity?

    lateinit var binding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater)
        return binding.root
    }

    // inflate is static in the ViewBinding class so we can't handle this ourselves
    // each fragment must call FragmentMyLayoutBinding.inflate(inflater)
    abstract fun inflate(@NonNull inflater: LayoutInflater): T
}