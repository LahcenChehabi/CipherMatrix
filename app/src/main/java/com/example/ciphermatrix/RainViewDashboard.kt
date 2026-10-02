package com.example.ciphermatrix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.hypot
import kotlin.random.Random

class RainViewDashboard(context: Context, attrs: AttributeSet?) : View(context, attrs) {


    private val greenPaint = Paint().apply {
        color = Color.parseColor("#00E676")
        textSize = 40f
        typeface = Typeface.MONOSPACE
        setShadowLayer(8f, 0f, 0f, Color.parseColor("#00E676"))
    }
    private val redPaint = Paint().apply {
        color = Color.parseColor("#FF1744")
        textSize = 45f
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(15f, 0f, 0f, Color.parseColor("#FF1744"))
    }


    private val networkPaint = Paint().apply {
        color = Color.parseColor("#3000E676")
        strokeWidth = 2f
        isAntiAlias = true
    }
    private val laserPaint = Paint().apply {
        color = Color.parseColor("#8000FF00")
        strokeWidth = 6f
        setShadowLayer(20f, 0f, 0f, Color.parseColor("#00FF00"))
    }


    private var columns = 0
    private lateinit var drops: FloatArray
    private lateinit var speeds: FloatArray
    private val standardChars = "010101XYZ0123456789$+-*/=%#&_<>[]ｱﾊｷﾎﾏﾑﾒﾓ".map { it.toString() }
    private val glitchSymbols = listOf("☠️", "ERROR!", "💀", "FAIL", "☣️", "0xDEAD", "☢️")


    class Node(var x: Float, var y: Float, var dx: Float, var dy: Float)
    private var nodes = Array(20) { Node(0f, 0f, 0f, 0f) }

    private var laserY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        columns = (w / 40f).toInt() + 1
        drops = FloatArray(columns) { Random.nextFloat() * height }
        speeds = FloatArray(columns) { Random.nextFloat() * 15f + 10f }


        nodes = Array(25) {
            Node(
                Random.nextFloat() * w, Random.nextFloat() * h,
                (Random.nextFloat() - 0.5f) * 4f, (Random.nextFloat() - 0.5f) * 4f
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in nodes.indices) {
            val n1 = nodes[i]
            n1.x += n1.dx
            n1.y += n1.dy

            if (n1.x <= 0 || n1.x >= width) n1.dx *= -1
            if (n1.y <= 0 || n1.y >= height) n1.dy *= -1

            canvas.drawCircle(n1.x, n1.y, 4f, networkPaint)

            for (j in i + 1 until nodes.size) {
                val n2 = nodes[j]
                val dist = hypot((n1.x - n2.x).toDouble(), (n1.y - n2.y).toDouble())
                if (dist < 250) {
                    networkPaint.alpha = (255 * (1 - dist / 250)).toInt() // كيبهات كلما بعدو
                    canvas.drawLine(n1.x, n1.y, n2.x, n2.y, networkPaint)
                }
            }
        }
        networkPaint.alpha = 50


        for (i in 0 until columns) {
            val isGlitch = Random.nextFloat() > 0.98f
            val text = if (isGlitch) glitchSymbols.random() else standardChars.random()
            val paint = if (isGlitch) redPaint else greenPaint

            canvas.drawText(text, i * 40f, drops[i], paint)
            drops[i] += speeds[i]
            if (drops[i] > height && Random.nextFloat() > 0.8f) {
                drops[i] = 0f
                speeds[i] = Random.nextFloat() * 15f + 10f
            }
        }


        canvas.drawLine(0f, laserY, width.toFloat(), laserY, laserPaint)
        laserY += 8f
        if (laserY > height) laserY = 0f

        postInvalidateDelayed(30)
    }
}