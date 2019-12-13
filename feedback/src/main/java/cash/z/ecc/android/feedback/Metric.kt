package cash.z.ecc.android.feedback

interface Metric {
    val name: String
}

data class TimeMetric(
    override val name: String,
    val times: MutableList<Long> = mutableListOf()
) : Metric {
    val startTime: Long?
        get() = times.firstOrNull()

    val endTime: Long?
        get() = times.lastOrNull()

    val elapsedTime: Long? get() = endTime?.minus(startTime ?: 0)

    fun markTime(): TimeMetric {
        times.add(System.currentTimeMillis())
        return this
    }
}
