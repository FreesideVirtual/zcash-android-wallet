package cash.z.ecc.android.feedback

import cash.z.ecc.android.feedback.util.CompositeJob
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.coroutines.coroutineContext

class Feedback(capacity: Int = 256) {
    lateinit var scope: CoroutineScope
        private set

    private val _metrics = BroadcastChannel<Metric>(capacity)
    private val _actions = BroadcastChannel<Action>(capacity)
    private var onStartListeners: MutableList<() -> Unit> = mutableListOf()

    private val jobs = CompositeJob()

    val metrics: Flow<Metric> = _metrics.asFlow()
    val actions: Flow<Action> = _actions.asFlow()

    /**
     * Verifies that this class is not leaking anything. Checks that all underlying channels are
     * closed and all launched reporting jobs are inactive.
     */
    val isStopped get() = ensureScope() && _metrics.isClosedForSend && _actions.isClosedForSend && !scope.isActive && !jobs.isActive()

    /**
     * Starts this feedback as a child of the calling coroutineContext, meaning when that context
     * ends, this feedback's scope and anything it launced will cancel. Note that the [metrics] and
     * [actions] channels will remain open unless [stop] is also called on this instance.
     */
    suspend fun start(): Feedback {
        check(!::scope.isInitialized) {
            "Error: cannot initialize feedback because it has already been initialized."
        }
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob(coroutineContext[Job]))
        invokeOnCompletion {
            _metrics.close()
            _actions.close()
        }
        onStartListeners.forEach { it() }
        onStartListeners.clear()
        return this
    }

    fun invokeOnCompletion(block: CompletionHandler) {
        ensureScope()
        scope.coroutineContext[Job]!!.invokeOnCompletion(block)
    }

    /**
     * Invokes the given callback after the scope has been initialized or immediately, if the scope
     * has already been initialized. This is used by [FeedbackCoordinator] and things like it that
     * want to immediately begin collecting the metrics/actions flows because any emissions that
     * occur before subscription are dropped.
     */
    fun onStart(onStartListener: () -> Unit) {
        if (::scope.isInitialized) {
            onStartListener()
        } else {
            onStartListeners.add(onStartListener)
        }
    }

    /**
     * Stop this instance and close all reporting channels. This function will first wait for all
     * in-flight reports to complete.
     */
    suspend fun stop() {
        // expose instances where stop is being called before start occurred.
        ensureScope()
        await()
        scope.cancel()
    }

    /**
     * Suspends until all in-flight reports have completed. This is automatically called before
     * [stop].
     */
    suspend fun await() {
        jobs.await()
    }

    /**
     * Measures the given block of code by surrounding it with time metrics and the reporting the
     * result.
     *
     * Don't measure code that launches coroutines, instead measure the code within the coroutine
     * that gets launched. Otherwise, the timing will be incorrect because the launched coroutine
     * will run concurrently--meaning a "happens before" relationship between the measurer and the
     * measured cannot be established and thereby the concurrent action cannot be timed.
     */
    inline fun <T> measure(key: String = "measurement.generic", description: Any = "measurement", block: () -> T): T {
        ensureScope()
        val metric = TimeMetric(key, description.toString()).markTime()
        val result = block()
        metric.markTime()
        report(metric)
        return result
    }

    /**
     * Add the given metric to the stream of metric events.
     *
     * @param metric the metric to add.
     */
    fun report(metric: Metric): Feedback {
        jobs += scope.launch {
            _metrics.send(metric)
        }
        return this
    }

    /**
     * Add the given action to the  stream of action events.
     *
     * @param action the action to add.
     */
    fun report(action: Action): Feedback {
        jobs += scope.launch {
            _actions.send(action)
        }
        return this
    }

    /**
     * Report the given error to everything that is tracking feedback. Converts it to a Crash object
     * which is intended for use in property-based analytics.
     *
     * @param error the uncaught exception that occurred.
     */
    fun report(error: Throwable?): Feedback {
        return report(Crash(error))
    }

    /**
     * Ensures that the scope for this instance has been initialized.
     */
    fun ensureScope(): Boolean {
        check(::scope.isInitialized) {
            "Error: feedback has not been initialized. Before attempting to use this feedback" +
                    " object, ensure feedback.start() has been called."
        }
        return true
    }

    fun ensureStopped(): Boolean {
        val errors = mutableListOf<String>()

        if (!_metrics.isClosedForSend && !_actions.isClosedForSend) errors += "both channels are still open"
        else if (!_actions.isClosedForSend) errors += "the actions channel is still open"
        else if (!_metrics.isClosedForSend) errors += "the metrics channel is still open"

        if (scope.isActive) errors += "the scope is still active"
        if (jobs.isActive()) errors += "reporting jobs are still active"
        if (errors.isEmpty()) return true
        throw IllegalStateException("Feedback is still active because ${errors.joinToString(", ")}.")
    }


    interface Metric : Mappable<String, Any> {
        val key: String
        val startTime: Long?
        val endTime: Long?
        val elapsedTime: Long?
        val description: String

        override fun toMap(): Map<String, Any> {
            return mapOf(
                "key" to key,
                "description" to description,
                "startTime" to (startTime ?: 0),
                "endTime" to (endTime ?: 0),
                "elapsedTime" to (elapsedTime ?: 0)
            )
        }
    }

    interface Action : Feedback.Mappable<String, Any> {
        val key: String
        override fun toMap(): Map<String, Any> {
            return mapOf("key" to key)
        }
    }

    interface Mappable<K, V> {
        fun toMap(): Map<K, V>
    }

    data class TimeMetric(
        override val key: String,
        override val description: String,
        val times: MutableList<Long> = mutableListOf()
    ) : Metric {
        override val startTime: Long? get() = times.firstOrNull()
        override val endTime: Long? get() = times.lastOrNull()
        override val elapsedTime: Long? get() = endTime?.minus(startTime ?: 0)
        fun markTime(): TimeMetric {
            times.add(System.currentTimeMillis())
            return this
        }

        override fun toString(): String {
            return "$description in ${elapsedTime}ms"
        }
    }

    data class Crash(val error: Throwable?) : Action {
        override val key: String = "crash"
        override fun toMap(): Map<String, Any> {
            return mutableMapOf<String, Any>(
                "message" to (error?.message ?: "None"),
                "cause" to (error?.cause?.toString() ?: "None"),
                "cause.cause" to (error?.cause?.cause?.toString() ?: "None"),
                "cause.cause.cause" to (error?.cause?.cause?.cause?.toString() ?: "None")
            ).apply { putAll(super.toMap()); putAll(error.stacktraceToMap()) }
        }
        override fun toString() = "App crashed due to: $error"
    }
}

private fun Throwable?.stacktraceToMap(chunkSize: Int = 250): Map<out String, String> {
    val properties = mutableMapOf("stacktrace0" to "None")
    if (this == null) return properties
    val stringWriter = StringWriter()

    printStackTrace(PrintWriter(stringWriter))

    stringWriter.toString().chunked(chunkSize).forEachIndexed { index, chunk ->
        properties["stacktrace$index"] = chunk
    }
    return properties
}
