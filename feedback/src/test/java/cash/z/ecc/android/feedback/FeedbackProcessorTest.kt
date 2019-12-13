package cash.z.ecc.android.feedback

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackProcessorTest {

    private val processors = mutableListOf<FeedbackProcessor>()
    private var counter: Int = 0
    private val simpleAction = object : Action {
        override val name = "ButtonClick"
    }

    @Test
    fun testConcurrency() = runBlocking {
        val actionCount = 40
        val processorCount = 40
        val expectedTotal = actionCount * processorCount

        val feedback = Feedback().start()
        repeat(processorCount) {
            createProcessor(feedback)
        }
        repeat(actionCount) {
            sendAction(feedback)
        }

        feedback.await()
        processors.forEach { it.await() }
        feedback.stop()
        assertEquals(
            "Concurrent modification happened ${expectedTotal - counter} times",
            expectedTotal,
            counter
        )
    }

    private fun createProcessor(feedback: Feedback) {
        processors += FeedbackProcessor(feedback) {
            counter++
        }
    }

    private fun sendAction(feedback: Feedback) {
        feedback.report(simpleAction)
    }

}