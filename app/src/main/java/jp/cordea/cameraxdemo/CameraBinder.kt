package jp.cordea.cameraxdemo

import android.graphics.Matrix
import android.media.Image
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class CameraBinder(
    private val owner: LifecycleOwner,
    private val textureView: TextureView
) {
    private val detector by lazy { FirebaseVision.getInstance().onDeviceTextRecognizer }

    fun start() {
        val preview = Preview(PreviewConfig.Builder().build())
        val analysis = ImageAnalysis(ImageAnalysisConfig.Builder().build()).apply {
            setAnalyzer { image, rotationDegrees ->
                image?.image?.let {
                    recognizeText(it, rotationDegrees)
                }
            }
        }

        preview.setOnPreviewOutputUpdateListener {
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            parent.addView(textureView, 0)
            textureView.surfaceTexture = it.surfaceTexture
            update()
        }

        CameraX.bindToLifecycle(owner, preview, analysis)
    }

    private fun update() {
        val display = textureView.display ?: return
        val x = textureView.width / 2f
        val y = textureView.height / 2f
        val rotationDegrees = display.rotation.fromRotation().toFloat()
        textureView.setTransform(
            Matrix().apply { postRotate(-rotationDegrees, x, y) }
        )
    }

    private fun recognizeText(image: Image, rotationDegrees: Int) {
        val visionImage = FirebaseVisionImage.fromMediaImage(image, rotationDegrees.toRotation())
        detector.processImage(visionImage)
            .addOnSuccessListener {
            }
    }

    private fun Int.toRotation() = when (this) {
        0 -> Surface.ROTATION_0
        90 -> Surface.ROTATION_90
        180 -> Surface.ROTATION_180
        270 -> Surface.ROTATION_270
        else -> throw IllegalStateException()
    }

    private fun Int.fromRotation() = when (this) {
        Surface.ROTATION_0 -> 0
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> throw IllegalStateException()
    }
}
