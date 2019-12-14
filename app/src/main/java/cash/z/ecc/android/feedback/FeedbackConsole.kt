package cash.z.ecc.android.feedback

import android.util.Log

class FeedbackConsole(coordinator: FeedbackCoordinator) : FeedbackCoordinator.FeedbackObserver {

    init {
        coordinator.addObserver(this)
    }

    override fun onMetric(metric: Feedback.Metric) {
        log(metric.toString())
    }

    override fun onAction(action: Feedback.Action) {
        log(action.toString())
    }

    override fun flush() {
        // TODO: flush logs (once we have the real logging in place)
    }

    private fun log(message: String) {
        Log.d("@TWIG", message)
    }
}