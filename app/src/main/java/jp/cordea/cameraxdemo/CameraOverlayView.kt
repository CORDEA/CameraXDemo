package jp.cordea.cameraxdemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

class CameraOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        color = context.getColor(R.color.colorAccent)
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    private var text: FirebaseVisionText? = null
    private var image: FirebaseVisionImage? = null

    fun update(image: FirebaseVisionImage, text: FirebaseVisionText) {
        this.image = image
        this.text = text
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val image = image ?: return
        val text = text ?: return
        val widthAspect = width / image.bitmap.width.toFloat()
        val heightAspect = height / image.bitmap.height.toFloat()
        text.textBlocks
            .flatMap { it.lines }
            .flatMap { it.elements }
            .filter { it.boundingBox != null }
            .forEach {
                val box = it.boundingBox!!
                canvas.drawRect(
                    box.left * widthAspect,
                    box.top * heightAspect,
                    box.right * widthAspect,
                    box.bottom * heightAspect,
                    paint
                )
            }
    }
}
