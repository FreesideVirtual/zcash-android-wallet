package cash.z.ecc.android.feedback

import cash.z.ecc.android.feedback.util.CompositeJob
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * Takes care of the boilerplate involved in processing feedback emissions. Simply provide callbacks
 * and emissions will occur in a mutually exclusive way, across all processors, so that things like
 * writing to a file can occur without clobbering changes. This class also provides a mechanism for
 * waiting for any in-flight emissions to complete. Lastly, all monitoring will cleanly complete
 * whenever the feedback is stopped or its parent scope is cancelled.
 */
class FeedbackProcessor(
    val feedback: Feedback,
    val onMetricListener: (Metric) -> Unit = {},
    val onActionListener: (Action) -> Unit = {}
) {

    init {
        feedback.onStart {
            initMetrics()
            initActions()
        }
    }

    private var contextMetrics = Dispatchers.IO
    private var contextActions = Dispatchers.IO
    private var jobs = CompositeJob()

    /**
     * Wait for any in-flight listeners to complete.
     */
    suspend fun await() {
        jobs.await()
    }

    fun metricsOn(dispatcher: CoroutineDispatcher): FeedbackProcessor {
        contextMetrics = dispatcher
        return this
    }

    fun actionsOn(dispatcher: CoroutineDispatcher): FeedbackProcessor {
        contextActions = dispatcher
        return this
    }

    private fun initMetrics() {
        feedback.metrics.onEach {
            jobs += feedback.scope.launch {
                withContext(contextMetrics) {
                    mutex.withLock {
                        onMetricListener(it)
                    }
                }
            }
        }.launchIn(feedback.scope)
    }

    private fun initActions() {
        feedback.actions.onEach {
            val id = coroutineContext.hashCode()
            jobs += feedback.scope.launch {
                withContext(contextActions) {
                    mutex.withLock {
                        onActionListener(it)
                    }
                }
            }
        }.launchIn(feedback.scope)
    }

    companion object {
        private val mutex: Mutex = Mutex()
    }
}
