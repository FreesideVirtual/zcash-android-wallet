package cash.z.ecc.android.feedback

import cash.z.ecc.android.ZcashWalletApp

enum class NonUserAction(override val key: String, val description: String) : Feedback.Action {
    FEEDBACK_STARTED("action.feedback.start", "feedback started"),
    FEEDBACK_STOPPED("action.feedback.stop", "feedback stopped");

    override fun toString(): String = description
}

enum class MetricType(override val key: String, val description: String) : Feedback.Action {
    SEED_CREATION("metric.seed.creation", "seed created")
}

class LaunchMetric private constructor(private val metric: Feedback.TimeMetric) :
    Feedback.Metric by metric {
    constructor() : this(
        Feedback
            .TimeMetric(
                "metric.app.launch",
                "app launched",
                mutableListOf(ZcashWalletApp.instance.creationTime)
            )
            .markTime()
    )
    override fun toString(): String = metric.toString()
}

fun <T> Feedback.measure(type: MetricType, block: () -> T) =
    this.measure(type.key, type.description, block)