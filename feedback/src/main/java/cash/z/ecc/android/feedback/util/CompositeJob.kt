package cash.z.ecc.android.feedback.util

import kotlinx.coroutines.Job

class CompositeJob {

    private val jobs = mutableListOf<Job>()
    val size: Int get() = jobs.size

    fun add(job: Job) {
        jobs.add(job)
        job.invokeOnCompletion {
            remove(job)
        }
    }

    fun remove(job: Job): Boolean {
        return jobs.remove(job)
    }

    fun isActive(): Boolean {
        return jobs.any { isActive() }
    }

    suspend fun await() {
        // allow for concurrent modification since the list isn't coroutine or thread safe
        do {
            val job = jobs.firstOrNull()
            if (job?.isActive == true) {
                job.join()
            } else {
                // prevents an infinite loop in the extreme edge case where the list has a null item
                try { jobs.remove(job) } catch (t: Throwable) {}
            }
        } while (size > 0)
    }

    fun cancel() {
        jobs.filter { isActive() }.forEach { it.cancel() }
    }

    operator fun plusAssign(also: Job) {
        add(also)
    }
}