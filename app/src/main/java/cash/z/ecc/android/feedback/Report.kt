package cash.z.ecc.android.feedback

import cash.z.ecc.android.ZcashWalletApp

enum class NonUserAction(override val key: String, val description: String) : Feedback.Action {
    FEEDBACK_STARTED("action.feedback.start", "feedback started"),
    FEEDBACK_STOPPED("action.feedback.stop", "feedback stopped");

    override fun toString(): String = description
}

class LaunchMetric private constructor(private val metric: Feedback.TimeMetric) :
    Feedback.Metric by metric {
    constructor() : this(
        Feedback
            .TimeMetric("metric.app.launch", mutableListOf(ZcashWalletApp.instance.creationTime))
            .markTime()
    )
    override fun toString(): String {
        return "app launched in ${metric.elapsedTime}ms"
    }
}
