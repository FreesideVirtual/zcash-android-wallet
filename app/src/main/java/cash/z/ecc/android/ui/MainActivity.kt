package cash.z.ecc.android.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import cash.z.ecc.android.R
import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.di.component.MainActivitySubcomponent
import cash.z.ecc.android.di.component.SynchronizerSubcomponent
import cash.z.ecc.android.feedback.Feedback
import cash.z.ecc.android.feedback.FeedbackCoordinator
import cash.z.ecc.android.feedback.LaunchMetric
import cash.z.ecc.android.feedback.Report.NonUserAction.FEEDBACK_STOPPED
import cash.z.ecc.android.feedback.Report.NonUserAction.SYNC_START
import cash.z.wallet.sdk.Initializer
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var feedback: Feedback

    @Inject
    lateinit var feedbackCoordinator: FeedbackCoordinator

    @Inject
    lateinit var clipboard: ClipboardManager


    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private var snackbar: Snackbar? = null

    lateinit var navController: NavController

    lateinit var component: MainActivitySubcomponent
    lateinit var synchronizerComponent: SynchronizerSubcomponent


    override fun onCreate(savedInstanceState: Bundle?) {
        component = ZcashWalletApp.component.mainActivitySubcomponent().create(this).also {
            it.inject(this)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        initNavigation()

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false)

        lifecycleScope.launch {
            feedback.start()
        }
    }

    override fun onResume() {
        super.onResume()
        // keep track of app launch metrics
        // (how long does it take the app to open when it is not already in the foreground)
        ZcashWalletApp.instance.let { app ->
            if (!app.creationMeasured) {
                app.creationMeasured = true
                feedback.report(LaunchMetric())
            }
        }
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            feedback.report(FEEDBACK_STOPPED)
            feedback.stop()
        }
        super.onDestroy()
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    private fun initNavigation() {
        navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, _, _ ->
            // hide the keyboard anytime we change destinations
            getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(
                this@MainActivity.window.decorView.rootView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun startSync(initializer: Initializer) {
        synchronizerComponent = ZcashWalletApp.component.synchronizerSubcomponent().create(initializer)
        feedback.report(SYNC_START)
        synchronizerComponent.synchronizer().start(lifecycleScope)
    }

    fun playSound(fileName: String) {
        mediaPlayer.apply {
            if (isPlaying) stop()
            try {
                reset()
                assets.openFd(fileName).let { afd ->
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                prepare()
                start()
            } catch (t: Throwable) {
                Log.e("SDK_ERROR", "ERROR: unable to play sound due to $t")
            }
        }
    }

    // TODO: spruce this up with API 26 stuff
    fun vibrateSuccess() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(longArrayOf(0, 200, 200, 100, 100, 800), -1)
        }
    }

    fun copyAddress(view: View? = null) {
        lifecycleScope.launch {
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    "Z-Address",
                    synchronizerComponent.synchronizer().getAddress()
                )
            )
            showMessage("Address copied!", "Sweet")
        }
    }

    fun preventBackPress(fragment: Fragment) {
        onFragmentBackPressed(fragment){}
    }

    fun onFragmentBackPressed(fragment: Fragment, block: () -> Unit) {
        onBackPressedDispatcher.addCallback(fragment, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                block()
            }
        })
    }

    private fun showMessage(message: String, action: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showSnackbar(message: String, action: String = "OK"): Snackbar {
        return if (snackbar == null) {
            val view = findViewById<View>(R.id.main_activity_container)
            val snacks = Snackbar
                .make(view, "$message", Snackbar.LENGTH_INDEFINITE)
                .setAction(action) { /*auto-close*/ }

                val snackBarView = snacks.view as ViewGroup
                val navigationBarHeight = resources.getDimensionPixelSize(resources.getIdentifier("navigation_bar_height", "dimen", "android"))
                val params = snackBarView.getChildAt(0).layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    navigationBarHeight
                )

                snackBarView.getChildAt(0).setLayoutParams(params)
            snacks
        } else {
            snackbar!!.setText(message).setAction(action) {/*auto-close*/}
        }.also {
            if (!it.isShownOrQueued) it.show()
        }
    }
}
