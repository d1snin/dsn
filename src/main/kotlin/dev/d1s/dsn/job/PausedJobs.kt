package dev.d1s.dsn.job

import org.quartz.JobKey

private const val SEPARATOR = ";"

data class PausedJobs(
    val jobs: MutableList<JobKey>
) {
    fun serialize() = jobs.joinToString(SEPARATOR) { it.name }

    companion object {

        fun deserialize(jobsString: String): PausedJobs {
            val jobs = jobsString.split(SEPARATOR).map { jobName ->
                JobKey(jobName)
            }

            return PausedJobs(jobs.toMutableList())
        }
    }
}
