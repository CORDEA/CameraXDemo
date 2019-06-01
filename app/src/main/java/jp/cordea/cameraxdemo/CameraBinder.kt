package jp.cordea.cameraxdemo

import android.graphics.Matrix
import android.media.Image
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class CameraBinder(
    private val owner: LifecycleOwner,
    private val textureView: TextureView,
    private val cameraOverlayView: CameraOverlayView
) {
    companion object {
        private const val HEIGHT = 640
        private const val WIDTH = 480
    }

    private val detector by lazy { FirebaseVision.getInstance().onDeviceTextRecognizer }
    private var isProcessing: Boolean = false

    fun start() {
        val rational = Rational(WIDTH, HEIGHT)
        val size = Size(WIDTH, HEIGHT)
        val preview = Preview(
            PreviewConfig.Builder()
                .setTargetResolution(size)
                .setTargetAspectRatio(rational)
                .build()
        )
        val analysis = ImageAnalysis(
            ImageAnalysisConfig.Builder()
                .setTargetResolution(size)
                .setTargetAspectRatio(rational)
                .build()
        ).apply {
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
            val textureSize = it.textureSize
            (textureView.layoutParams as ConstraintLayout.LayoutParams).applyDimensionRatio(textureSize)
            (cameraOverlayView.layoutParams as ConstraintLayout.LayoutParams).applyDimensionRatio(textureSize)
            update()
        }

        CameraX.bindToLifecycle(owner, preview, analysis)
    }

    private fun ConstraintLayout.LayoutParams.applyDimensionRatio(size: Size) {
        dimensionRatio = "H,${size.height}:${size.width}"
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
        if (isProcessing) {
            return
        }
        isProcessing = true
        val visionImage = FirebaseVisionImage.fromMediaImage(image, rotationDegrees.toRotation())
        detector.processImage(visionImage)
            .addOnSuccessListener {
                cameraOverlayView.update(visionImage, it)
                isProcessing = false
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
