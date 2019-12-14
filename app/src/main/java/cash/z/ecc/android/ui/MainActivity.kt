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
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import cash.z.ecc.android.R
import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.di.annotation.ActivityScope
import cash.z.ecc.android.feedback.Feedback
import cash.z.ecc.android.feedback.LaunchMetric
import cash.z.ecc.android.feedback.NonUserAction.FEEDBACK_STOPPED
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var feedback: Feedback

    lateinit var navController: NavController

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        initNavigation()

        window.statusBarColor = Color.TRANSPARENT;
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setWindowFlag(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            false
        )// | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false)

        lifecycleScope.launch { feedback.start() }
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

    fun copyAddress(view: View) {
        // TODO: get address from synchronizer
        val address =
            "zs1qduvdyuv83pyygjvc4cfcuc2wj5flnqn730iigf0tjct8k5ccs9y30p96j2gvn9gzyxm6q0vj12c4"
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                "Z-Address",
                address
            )
        )
        showMessage("Address copied!", "Sweet")
    }

    private fun showMessage(message: String, action: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

@Module
abstract class MainActivityModule {
    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeActivity(): MainActivity
}