package com.screenlight.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class AnalogClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private val dateFormat = SimpleDateFormat("EEE d", Locale.US)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = (Math.min(w, h) / 2f) * 0.85f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // Draw Clock Face (Subtle ring)
        paint.color = Color.parseColor("#1AFFFFFF") // border-neutral-800/50
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw Major Ticks (12, 3, 6, 9)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#737373") // bg-neutral-500
        val tickWidth = 4f
        val tickLengthMajor = 30f
        val tickCornerRadius = 2f

        for (i in 0 until 4) {
            canvas.save()
            canvas.rotate(i * 90f, centerX, centerY)
            val rect = RectF(centerX - tickWidth / 2, centerY - radius + 10f, centerX + tickWidth / 2, centerY - radius + 10f + tickLengthMajor)
            canvas.drawRoundRect(rect, tickCornerRadius, tickCornerRadius, paint)
            canvas.restore()
        }

        // Draw Minor Dots (Every 30 degrees, skipping major ticks)
        paint.color = Color.parseColor("#262626") // bg-neutral-800
        val dotRadius = 4f
        val dotDistanceFromCenter = radius - 25f
        for (i in 0 until 12) {
            if (i % 3 == 0) continue 
            val angle = Math.toRadians((i * 30).toDouble())
            val dotX = centerX + (dotDistanceFromCenter * sin(angle)).toFloat()
            val dotY = centerY - (dotDistanceFromCenter * cos(angle)).toFloat()
            canvas.drawCircle(dotX, dotY, dotRadius, paint)
        }

        // Draw Date Text
        paint.color = Color.parseColor("#525252") // text-neutral-600
        paint.textSize = 32f
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = 0.2f
        paint.isFakeBoldText = true
        val dateText = dateFormat.format(calendar.time).uppercase()
        canvas.drawText(dateText, centerX, centerY + (radius * 0.4f), paint)

        // Draw Hands
        // Hour Hand (Off-White, Thick)
        drawHand(canvas, (hour + minute / 60f) * 30.0, radius * 0.65f, 18f, Color.parseColor("#E5E5E5"), 0.85f)
        
        // Minute Hand (Grayish, Medium)
        drawHand(canvas, (minute + second / 60f) * 6.0, radius * 0.85f, 12f, Color.parseColor("#A3A3A3"), 0.85f)
        
        // Second Hand (Primary Color, Thin)
        drawHand(canvas, second * 6.0, radius * 0.95f, 5f, Color.parseColor("#0a3743"), 0.85f)

        // Center Pin
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#262626") // bg-neutral-800
        canvas.drawCircle(centerX, centerY, 14f, paint)
        paint.color = Color.parseColor("#0a3743") // border-primary
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(centerX, centerY, 14f, paint)

        postInvalidateDelayed(1000)
    }

    private fun drawHand(canvas: Canvas, angleDegrees: Double, length: Float, thickness: Float, color: Int, pivotRatio: Float) {
        paint.color = color
        paint.strokeWidth = thickness
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE
        
        val angle = Math.toRadians(angleDegrees)
        
        val tailLength = length * (1 - pivotRatio)
        val startX = centerX - (tailLength * sin(angle)).toFloat()
        val startY = centerY + (tailLength * cos(angle)).toFloat()
        
        val endX = centerX + (length * pivotRatio * sin(angle)).toFloat()
        val endY = centerY - (length * pivotRatio * cos(angle)).toFloat()
        
        canvas.drawLine(startX, startY, endX, endY, paint)
    }
}