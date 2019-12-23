package cash.z.ecc.android.feedback

import cash.z.ecc.android.ZcashWalletApp

object Report {
    enum class NonUserAction(override val key: String, val description: String) : Feedback.Action {
        FEEDBACK_STARTED("action.feedback.start", "feedback started"),
        FEEDBACK_STOPPED("action.feedback.stop", "feedback stopped"),
        SYNC_START("action.feedback.synchronizer.start", "sync started");

        override fun toString(): String = description
    }

    enum class MetricType(override val key: String, val description: String) : Feedback.Action {
        ENTROPY_CREATED("metric.entropy.created", "entropy created"),
        SEED_CREATED("metric.seed.created", "seed created"),
        SEED_IMPORTED("metric.seed.imported", "seed imported"),
        SEED_PHRASE_CREATED("metric.seedphrase.created", "seed phrase created"),
        SEED_PHRASE_LOADED("metric.seedphrase.loaded", "seed phrase loaded"),
        WALLET_CREATED("metric.wallet.created", "wallet created"),
        WALLET_IMPORTED("metric.wallet.imported", "wallet imported"),
        ACCOUNT_CREATED("metric.account.created", "account created")
    }
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

inline fun <T> Feedback.measure(type: Report.MetricType, block: () -> T): T =
    this.measure(type.key, type.description, block)