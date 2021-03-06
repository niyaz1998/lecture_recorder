package com.example.lecturerecorder.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lecturerecorder.model.ListService
import com.example.lecturerecorder.model.NotePost
import com.example.lecturerecorder.model.NoteResponse
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

/*
сервис запускается для загрузки лекции на сервак
сначла сервис загружает лекцию (создание лекции происходит сразу вместе с загрузкой файла)
после он догружает заметки для лекции по одной
когда закончит он отсылает интент в LocalBroadcast (чтобы обновить список лекций)
 */
class AudioUploadService : JobIntentService() {
    var mDisposable: Disposable? = null

    var name: String? = null
    var notes: List<NoteResponse>? = null

    override fun onHandleWork(@NonNull intent: Intent) {
        /**
         * Download/Upload of file
         * The system or framework is already holding a wake lock for us at this point
         */
        // get file file here
        val mFilePath = intent.getStringExtra("mFilePath")
        val courseId = intent.getIntExtra("courseId", -1)
        val name = intent.getStringExtra("name")
        notes = intent.getParcelableArrayListExtra<NoteResponse>("notes")
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
                val response = apiService.createLecture(
                    createRequestBodyFromText(courseId.toString()),
                    createRequestBodyFromText(name),
                    createMultipartBody(mFilePath, emitter)
                ).blockingGet()
                emitter.onComplete()
                sendNotes(response.id)
                onLectureCreated()
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

    private fun onLectureCreated() {
        val localIntent = Intent("com.example.lecturerecorder.AudioUploadService")
        localIntent.putExtra("message", "lecture_created")
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }

    private fun onErrors(throwable: Throwable) {
        Log.e(TAG, "onErrors: ", throwable)
    }

    private fun onProgress(progress: Double) {
        Log.i(TAG, "onProgress: $progress")
    }

    private fun onSuccess() {
        Log.i(TAG, "onSuccess: File Uploaded")
    }

    private fun sendNotes(lectureId: Int) {
        notes?.forEach {
            var note: NoteResponse? = null
            try {
                 note = RestClient.listService.createNote(
                    lectureId,
                    NotePost(lectureId, text = it.text, timestamp = it.timestamp, picture = null)
                ).blockingGet()
                Log.d(TAG, "Note added ${note.text}")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun createRequestBodyFromFile(file: File, mimeType: String): RequestBody {
        return RequestBody.create(MediaType.parse(mimeType), file)
    }

    private fun createRequestBodyFromText(mText: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), mText)
    }

    private fun createMultipartBody(
        filePath: String,
        emitter: FlowableEmitter<Double>
    ): MultipartBody.Part {
        val file = File(filePath)
        return MultipartBody.Part.createFormData(
            "file", file.name,
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
        private const val TAG = "AudioUploadService"

        private const val JOB_ID = 102
        fun enqueueWork(context: Context, intent: Intent?) {
            enqueueWork(
                context,
                AudioUploadService::class.java, JOB_ID, intent!!
            )
        }
    }
}