package com.github.xe11.camxdemo.camera.capture.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = 0xFFFF0000.toInt() // TODO [Alexei Laban]: set color from Attrs
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val faceBounds = mutableListOf<Rect>()

    fun updateFaceBounds(boundsInPercents: List<Rect>) {
        faceBounds.clear()

        val boundsInPixels = boundsInPercents.map { rect ->
            val left = width - (rect.right * width / 100)
            val right = width - (rect.left * width / 100)
            val top = (rect.top * height / 100)
            val bottom = (rect.bottom * height / 100)
            rect.set(left, top, right, bottom)

            rect
        }

        faceBounds.addAll(boundsInPixels)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        faceBounds.forEach { rect ->
            canvas.drawRect(rect, paint)
        }
    }
}
