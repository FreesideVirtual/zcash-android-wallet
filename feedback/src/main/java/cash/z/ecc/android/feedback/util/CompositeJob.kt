package cash.z.ecc.android.feedback.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

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
        var activeJobs = jobs.filter { it.isActive }
        while (activeJobs.isNotEmpty()) {
            // allow for concurrent modification since the list isn't coroutine or thread safe
            repeat(jobs.size) {
                if (it < jobs.size) jobs[it].join()
            }
            delay(100)
            activeJobs = jobs.filter { it.isActive }
        }
    }

    operator fun plusAssign(also: Job) {
        add(also)
    }
}