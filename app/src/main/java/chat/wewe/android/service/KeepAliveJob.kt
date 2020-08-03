package chat.wewe.android.service

import chat.wewe.android.ConnectionStatusManager
import chat.wewe.android.RocketChatApplication
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit

class KeepAliveJob : Job() {

    private val connectivityManager: ConnectivityManagerApi

    companion object {
        val TAG = "chat.wewe.android.service.KeepAliveJob"
        fun schedule() {
            JobRequest.Builder(TAG)
                    .setExecutionWindow(TimeUnit.SECONDS.toMillis(3L), TimeUnit.SECONDS.toMillis(10L))
                    .setBackoffCriteria(10L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setUpdateCurrent(true)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequiresCharging(false)
                    .setRequirementsEnforced(true)
                    .build()
                    .schedule()
        }

        fun cancelPendingJobRequests() {
            val allJobRequests = JobManager.instance().getAllJobRequestsForTag(TAG)
            allJobRequests.forEach { jobRequest ->
                jobRequest.cancelAndEdit()
            }
        }
    }

    init {
        val context = RocketChatApplication.getInstance()
        connectivityManager = ConnectivityManager.getInstance(context)
    }

    override fun onRunJob(params: Params): Result {
        if (ConnectionStatusManager.transitionCount() == 0L) {
            return Result.SUCCESS
        }
        when (ConnectionStatusManager.currentState()) {
            ConnectionStatusManager.State.CONNECTING, ConnectionStatusManager.State.ONLINE -> {
                cancel()
            }
            else -> connectivityManager.keepAliveServer()
        }
        return Result.SUCCESS
    }
}