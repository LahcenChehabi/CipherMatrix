package com.example.ciphermatrix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class ErrorMatrixView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private val paint = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    private val errorPool = arrayOf(
        "ERROR", "CRITICAL", "404", "FAIL", "⚠", "SYSTEM_COMPROMISED",
        "0", "1", "X", "ACCESS_DENIED", "BYPASS", "KERNEL_PANIC", "☠"
    )

    private var columnWidth = 60
    private var fontSize = 35f
    private var columns = 0

    private lateinit var txtPositions: IntArray
    private lateinit var columnSpeeds: IntArray
    private lateinit var currentWords: Array<String>

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        columns = w / columnWidth
        txtPositions = IntArray(columns) { Random.nextInt(-h, 0) }
        columnSpeeds = IntArray(columns) { Random.nextInt(25, 55) }
        currentWords = Array(columns) { errorPool[Random.nextInt(errorPool.size)] }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (columns == 0) return

        canvas.drawColor(Color.parseColor("#E6000000"))

        for (i in 0 until columns) {
            val word = currentWords[i]

            if (word == "ERROR" || word == "CRITICAL" || word == "⚠" || word == "☠" || word == "KERNEL_PANIC") {
                paint.color = Color.parseColor("#FF0033") // Neon Red
                paint.textSize = fontSize + 5f
            } else {
                paint.color = Color.parseColor("#00FF00") // Neon Green
                paint.textSize = fontSize
            }

            val x = i * columnWidth + 10f
            val y = txtPositions[i].toFloat()
            canvas.drawText(word, x, y, paint)

            txtPositions[i] += columnSpeeds[i]

            if (txtPositions[i] > height) {
                txtPositions[i] = Random.nextInt(-200, 0)
                columnSpeeds[i] = Random.nextInt(25, 55)
                currentWords[i] = errorPool[Random.nextInt(errorPool.size)]
            }
        }

        postInvalidateDelayed(30)
    }
}