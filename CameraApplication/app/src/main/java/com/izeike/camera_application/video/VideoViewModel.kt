package com.izeike.camera_application.video

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import com.izeike.camera_application.constant.Path
import java.text.SimpleDateFormat
import java.util.Locale

class VideoViewModel(app: Application): AndroidViewModel(app) {

    private val context: Context
        get() = getApplication<Application>().applicationContext

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    var isStartRecord by mutableStateOf(false)
        private set

    var isPauseRecord by mutableStateOf(false)
        private set

    var cameraSelector by mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
        private set

    private val videoConsumer = Consumer<VideoRecordEvent> { event ->
        when(event) {
            is VideoRecordEvent.Start -> {
                Toast.makeText(context,"start record video", Toast.LENGTH_SHORT).show()
            }
            is VideoRecordEvent.Finalize -> {
                if(event.hasError()) {
                    Log.d("video-error", "record video failed: ${event.cause?.message}")
                    Toast.makeText(
                        context,
                        "record video failed: ${event.cause?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val videoUri = event.outputResults.outputUri
                    Toast.makeText(
                        context,
                        "record video success: $videoUri",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            is VideoRecordEvent.Status -> {
                // record in progress
            }
            is VideoRecordEvent.Pause -> {
                Toast.makeText(context,"pause video", Toast.LENGTH_SHORT).show()
            }
            is VideoRecordEvent.Resume -> {
                Toast.makeText(context,"resume video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun changeCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    fun startRecording() {
        if(isStartRecord) {
            isStartRecord = false
            recording?.stop()
        } else {
            isStartRecord = true
            createRecording()
        }
    }

    fun pauseRecording() {
        if(isPauseRecord) {
            isPauseRecord = false
            recording?.resume()
        } else {
            isPauseRecord = true
            recording?.pause()
        }
    }

    private fun createRecording() {
        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISC_NUMBER, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.VideoColumns.RELATIVE_PATH, Path.APP_VIDEOS_PATH)
        }

        val outputOption = MediaStoreOutputOptions
            .Builder(
                context.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            .setContentValues(contentValues)
            .build()

        recording = videoCapture?.output
            ?.prepareRecording(context, outputOption)
            ?.start(ContextCompat.getMainExecutor(context),videoConsumer)
    }

    suspend fun setUpVideoCapture(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector
    ) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()

        val preview = Preview.Builder()
            .build()
            .apply { setSurfaceProvider(previewView.surfaceProvider) }

        val qualitySelector = QualitySelector.from(
            Quality.FHD,
            FallbackStrategy.lowerQualityOrHigherThan(Quality.FHD)
        )
        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(context))
            .setQualitySelector(qualitySelector)
            .build()

        val videoCapture = VideoCapture.withOutput(recorder)

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            videoCapture
        )

        this.videoCapture = videoCapture
    }
}