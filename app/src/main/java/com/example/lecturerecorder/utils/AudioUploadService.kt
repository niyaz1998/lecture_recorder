package com.example.lecturerecorder.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lecturerecorder.model.ListService
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class AudioUploadService : JobIntentService() {
    var mDisposable: Disposable? = null

    var name: String? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onHandleWork(@NonNull intent: Intent) {
        /**
         * Download/Upload of file
         * The system or framework is already holding a wake lock for us at this point
         */
        // get file file here
        val mFilePath = intent.getStringExtra("mFilePath")
        val courseId = intent.getIntExtra("courseId", -1)
        val name = intent.getStringExtra("name")
        if (mFilePath == null || courseId == -1 || name == null) {
            Log.e(
                TAG,
                "onHandleWork: Invalid input: filename = $mFilePath; courseId = $courseId; name = $name"
            )
            return
        }
        val apiService: ListService = RestClient.listService
        val fileObservable = Flowable.create(
            { emitter: FlowableEmitter<Double> ->
                try {
                    apiService.createLecture(
                        /*
                    mapOf(
                        "courseId" to createRequestBodyFromText(courseId.toString()),
                        "name" to createRequestBodyFromText(name)
                    ),
                     // courseId, name,
                     */
                        createRequestBodyFromText(courseId.toString()),
                        createRequestBodyFromText(name),
                        createMultipartBody(mFilePath, emitter)
                    ).blockingGet()
                } catch (e: Throwable) {
                    Log.e(TAG, e.localizedMessage)
                }
                emitter.onComplete()
            }, BackpressureStrategy.LATEST
        )
        mDisposable = fileObservable.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { progress: Double ->
                    onProgress(
                        progress
                    )
                },
                { throwable: Throwable ->
                    onErrors(
                        throwable
                    )
                }
            ) { onSuccess() }
    }

    private fun onErrors(throwable: Throwable) {
        sendBroadcastMeaasge("Error in file upload " + throwable.message)
        Log.e(TAG, "onErrors: ", throwable)
    }

    private fun onProgress(progress: Double) {
        sendBroadcastMeaasge("Uploading in progress... " + (100 * progress).toInt())
        Log.i(TAG, "onProgress: $progress")
    }

    private fun onSuccess() {
        sendBroadcastMeaasge("File uploading successful ")
        Log.i(TAG, "onSuccess: File Uploaded")
    }

    fun sendBroadcastMeaasge(message: String?) {
        val localIntent = Intent("my.own.broadcast")
        localIntent.putExtra("result", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }

    private fun createRequestBodyFromFile(file: File, mimeType: String): RequestBody {
        return RequestBody.create(MediaType.parse(mimeType), file)
    }

    private fun createRequestBodyFromText(mText: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), mText)
    }

    /**
     * return multi part body in format of FlowableEmitter
     */
    private fun createMultipartBody(
        filePath: String,
        emitter: FlowableEmitter<Double>
    ): MultipartBody.Part {
        val file = File(filePath)
        return MultipartBody.Part.createFormData(
            "myFile", file.name,
            createCountingRequestBody(file, "audio", emitter)
        )
    }

    private fun createCountingRequestBody(
        file: File, mimeType: String,
        emitter: FlowableEmitter<Double>
    ): RequestBody {
        val requestBody = createRequestBodyFromFile(file, mimeType)
        return CountingRequestBody(
            requestBody,
            object : CountingRequestBody.Listener {
                override fun onRequestProgress(bytesWritten: Long, contentLength: Long) {
                    val progress = 1.0 * bytesWritten / contentLength
                    emitter.onNext(progress)
                }
            }
        )
    }

    companion object {
        private const val TAG = "FileUploadService"

        /**
         * Unique job ID for this service.
         */
        private const val JOB_ID = 102
        fun enqueueWork(context: Context, intent: Intent?) {
            enqueueWork(
                context,
                AudioUploadService::class.java, JOB_ID, intent!!
            )
        }
    }
}