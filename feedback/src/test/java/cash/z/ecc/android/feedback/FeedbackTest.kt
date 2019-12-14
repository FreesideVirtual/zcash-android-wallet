package cash.z.ecc.android.feedback

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackTest {

    @Test
    fun testMeasure_blocking() = runBlocking {
        val duration = 1_100L
        val feedback = Feedback().start()
        verifyDuration(feedback, duration)

        feedback.measure {
            workBlocking(duration)
        }
    }

    @Test
    fun testMeasure_suspending() = runBlocking {
        val duration = 1_100L
        val feedback = Feedback().start()
        verifyDuration(feedback, duration)

        feedback.measure {
            workSuspending(duration)
        }
    }

    @Test
    fun testTrack() = runBlocking {
        val simpleAction = object : Feedback.Action {
            override val key = "ButtonClick"
        }
        val feedback = Feedback().start()
        verifyAction(feedback, simpleAction.key)

        feedback.report(simpleAction)
    }

    @Test
    fun testCancellation_stop() = runBlocking {
        verifyFeedbackCancellation { feedback, _ ->
            feedback.stop()
        }
    }

    @Test
    fun testCancellation_cancel() = runBlocking {
        verifyFeedbackCancellation { _, parentJob ->
            parentJob.cancel()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun testCancellation_noCancel() = runBlocking {
        verifyFeedbackCancellation { _, _ -> }
    }

    private fun verifyFeedbackCancellation(testBlock: suspend (Feedback, Job) -> Unit) = runBlocking {
        val feedback = Feedback()
        var counter = 0
        val parentJob = launch {
            feedback.start()
            feedback.scope.launch {
                delay(50)
                counter = 1
            }
        }
        // give feedback.start a chance to happen before cancelling
        delay(25)
        // stop or cancel things here
        testBlock(feedback, parentJob)
        delay(75)
        feedback.ensureStopped()
        assertEquals(0, counter)
    }

    private fun verifyDuration(feedback: Feedback, duration: Long) {
        feedback.metrics.onEach {
            val metric = (it as? Feedback.TimeMetric)?.elapsedTime
            assertTrue(
                "Measured time did not match duration. Expected $duration but was $metric",
                metric ?: 0 >= duration
            )
            feedback.stop()
        }.launchIn(feedback.scope)
    }

    private fun verifyAction(feedback: Feedback, name: String) {
        feedback.actions.onEach {
            assertTrue("Action did not match. Expected $name but was ${it.key}", name == it.key)
            feedback.stop()
        }.launchIn(feedback.scope)
    }

    private fun workBlocking(duration: Long) {
        Thread.sleep(duration)
    }

    private suspend fun workSuspending(duration: Long) {
        delay(duration)
    }
}