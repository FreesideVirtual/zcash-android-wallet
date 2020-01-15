package cash.z.ecc.android.ui.home

import android.animation.ValueAnimator
import cash.z.wallet.sdk.ext.twig
import com.airbnb.lottie.LottieAnimationView

class MagicSnakeLoader(
    val lottie: LottieAnimationView,
    private val scanningStartFrame: Int = 100,
    private val scanningEndFrame: Int = 175,
    val totalFrames: Int = 200
) : ValueAnimator.AnimatorUpdateListener {
    private var isPaused: Boolean = true
    private var isStarted: Boolean = false

    var isSynced: Boolean = false
        set(value) {
            twig("ZZZ isSynced=$value  isStarted=$isStarted")
            if (value && !isStarted) {
                twig("ZZZ isSynced=$value    TURBO sync")
                lottie.progress = 1.0f
                field = value
                return
            }

            // it is started but it hadn't reached the synced state yet
            if (value && !field) {
                twig("ZZZ synced was $field but now is $value so playing to completion since we are now synced")
                field = value
                playToCompletion()
            } else {
                field = value
                twig("ZZZ isSynced=$value and lottie.progress=${lottie.progress}")
            }
        }

    var scanProgress: Int = 0
        set(value) {
            field = value
            twig("ZZZ scanProgress=$value")
            if (value > 0) {
                startMaybe()
                onScanUpdated()
            }
        }

    var downloadProgress: Int = 0
        set(value) {
            field = value
            twig("ZZZ downloadProgress=$value")
            if (value > 0) startMaybe()
        }

    private fun startMaybe() {

        if (!isSynced && !isStarted) lottie.postDelayed({
            // after some delay, if we're still not synced then we better start animating (unless we already are)!
            if (!isSynced && isPaused) {
                twig("ZZZ yes start!")
                lottie.resumeAnimation()
                isPaused = false
                isStarted = true
            } else {
                twig("ZZZ I would have started but we're already synced!")
            }
        }, 200L).also {  twig("ZZZ startMaybe???") }
    }
//        set(value) {
//            field = value
//            if (value in 1..99 && isStopped) {
//                lottie.playAnimation()
//                isStopped = false
//            } else if (value >= 100) {
//                isStopped = true
//            }
//        }


    private val isDownloading get() = downloadProgress in 1..99
    private val isScanning get() = scanProgress in 1..99

    init {
        lottie.addAnimatorUpdateListener(this)
    }

    //        downloading = true
//    lottieAnimationView.playAnimation()
//    lottieAnimationView.addAnimatorUpdateListener { valueAnimator ->
//        // Set animation progress
//        val progress = (valueAnimator.animatedValue as Float * 100).toInt()
//        progressTv.text = "Progress: $progress%"
//
//        if (downloading && progress >= 40) {
//            lottieAnimationView.progress = 0f
//        }
//    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        if (isSynced || isPaused) {
//            playToCompletion()
            return
        }
        twig("ZZZ")
        twig("ZZZ\t\tonAnimationUpdate(${animation.animatedValue})")

        // if we are scanning, then set the animation progress, based on the scan progress
        // if we're not scanning, then we're looping
        animation.currentFrame().let { frame ->
            if (isDownloading) allowLoop(frame) else applyScanProgress(frame)
        }
    }

    private val acceptablePauseFrames = arrayOf(33,34,67,68,99)
    private fun applyScanProgress(frame: Int) {
        twig("ZZZ applyScanProgress($frame) : isPaused=$isPaused  isStarted=$isStarted  min=${lottie.minFrame}   max=${lottie.maxFrame}")
        // don't hardcode the progress until the loop animation has completed, cleanly
        if (isPaused) {
            onScanUpdated()
        } else {
            // once we're ready to show scan progress, do it! Don't do extra loops.
            if (frame >= scanningStartFrame || frame in acceptablePauseFrames) {
                twig("ZZZ pausing so we can scan!  ${if(frame<scanningStartFrame) "WE STOPPED EARLY!" else ""}")
                pause()
            }
        }
    }

    private fun onScanUpdated() {
        twig("ZZZ onScanUpdated : isPaused=$isPaused")
        if (isSynced) {
//            playToCompletion()
            return
        }

        if (isPaused && isStarted) {
            // move forward within the scan range, proportionate to how much scanning is complete
            val scanRange = scanningEndFrame - scanningStartFrame
            val scanRangeProgress = scanProgress.toFloat() / 100.0f * scanRange.toFloat()
            lottie.progress = (scanningStartFrame.toFloat() + scanRangeProgress) / totalFrames
            twig("ZZZ onScanUpdated : scanRange=$scanRange  scanRangeProgress=$scanRangeProgress  lottie.progress=${(scanningStartFrame.toFloat() + scanRangeProgress)}/$totalFrames=${lottie.progress}")
        }
    }

    private fun playToCompletion() {
        removeLoops()
        twig("ZZZ playing to completion")
        unpause()
    }

    private fun removeLoops() {
        lottie.frame.let {frame ->
            if (frame in 33..67) {
                twig("ZZZ removing 1 loop!")
                lottie.frame = frame + 34
            } else if (frame in 0..33) {
                twig("ZZZ removing 2 loops!")
                lottie.frame = frame + 67
            }
        }
    }

    private fun allowLoop(frame: Int) {
        twig("ZZZ allowLoop($frame) : isPaused=$isPaused")
        unpause()
        if (frame >= scanningStartFrame) {
            twig("ZZZ resetting to 0f (LOOPING)")
            lottie.progress = 0f
        }
    }

    fun unpause() {
        if (isPaused) {
            twig("ZZZ unpausing")
            lottie.resumeAnimation()
            isPaused = false
        }
    }

    fun pause() {
        if (!isPaused) {
            twig("ZZZ pausing")
            lottie.pauseAnimation()
            isPaused = true
        }
    }

    private fun ValueAnimator.currentFrame(): Int {
        return ((animatedValue as Float) * totalFrames).toInt()
    }
}

