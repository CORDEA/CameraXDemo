package jp.cordea.cameraxdemo

import android.graphics.Matrix
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.lifecycle.LifecycleOwner

class CameraBinder(
    private val owner: LifecycleOwner,
    private val textureView: TextureView
) {
    fun start() {
        val preview = Preview(PreviewConfig.Builder().build())

        preview.setOnPreviewOutputUpdateListener {
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            parent.addView(textureView, 0)
            textureView.surfaceTexture = it.surfaceTexture
            update()
        }

        CameraX.bindToLifecycle(owner, preview)
    }

    private fun update() {
        val display = textureView.display ?: return
        val x = textureView.width / 2f
        val y = textureView.height / 2f
        val rotationDegrees = when (display.rotation) {
            Surface.ROTATION_0 -> 0f
            Surface.ROTATION_90 -> 90f
            Surface.ROTATION_180 -> 180f
            Surface.ROTATION_270 -> 270f
            else -> throw IllegalStateException()
        }
        textureView.setTransform(
            Matrix().apply { postRotate(-rotationDegrees, x, y) }
        )
    }
}
