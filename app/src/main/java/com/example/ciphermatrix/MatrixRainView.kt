package com.example.ciphermatrix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Random

class MatrixRainView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    private val random = Random()


    private val chars = "01010123456789".toCharArray()

    private var dropY = FloatArray(0)
    private var speeds = FloatArray(0)
    private val fontSize = 42f

    init {
        paint.isAntiAlias = true
        paint.textSize = fontSize

        paint.isFakeBoldText = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val columns = (w / fontSize).toInt()

        dropY = FloatArray(columns)
        speeds = FloatArray(columns)

        for (i in 0 until columns) {
            dropY[i] = (-random.nextInt(h + 100)).toFloat()
            speeds[i] = (12..28).random().toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        canvas.drawColor(Color.parseColor("#050505"))

        for (i in dropY.indices) {

            for (j in 0..12) {
                val yPos = dropY[i] - (j * fontSize)


                if (yPos < 0 || yPos > height + fontSize) continue

                if (j == 0) {

                    paint.color = Color.WHITE
                    paint.setShadowLayer(14f, 0f, 0f, Color.GREEN)
                } else {

                    val alpha = (255 * (1f - j / 13f)).toInt().coerceIn(0, 255)
                    paint.color = Color.GREEN
                    paint.alpha = alpha
                    paint.clearShadowLayer()
                }


                val text = chars[random.nextInt(chars.size)].toString()
                canvas.drawText(text, i * fontSize, yPos, paint)
            }


            dropY[i] += speeds[i]

            //
            if (dropY[i] - (12 * fontSize) > height) {
                dropY[i] = -fontSize
                speeds[i] = (12..28).random().toFloat() //
            }
        }

        //
        postInvalidateDelayed(16)
    }
}