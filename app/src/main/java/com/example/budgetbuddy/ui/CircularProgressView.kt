package com.example.budgetbuddy.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.budgetbuddy.R

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _progress = 0f
    var progress: Float
        get() = _progress
        set(value) {
            _progress = value
            invalidate()
        }

    private var _maxProgress = 100f
    var maxProgress: Float
        get() = _maxProgress
        set(value) {
            _maxProgress = value
            invalidate()
        }

    private var _centerText = ""
    var centerText: String
        get() = _centerText
        set(value) {
            _centerText = value
            invalidate()
        }

    private var _isReverse = false
    var isReverse: Boolean
        get() = _isReverse
        set(value) {
            _isReverse = value
            invalidate()
        }

    private var progressColor = ContextCompat.getColor(context, R.color.primary)
    
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EEEEEE")
        style = Paint.Style.STROKE
        strokeWidth = 36f
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = progressColor
        style = Paint.Style.STROKE
        strokeWidth = 36f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val size = if (width < height) width else height
        val padding = trackPaint.strokeWidth / 2 + 15
        
        rect.set(padding, padding, size - padding, size - padding)
        
        // Draw track
        canvas.drawOval(rect, trackPaint)
        
        // Calculate progress ratio
        val ratio = if (maxProgress > 0) (progress / maxProgress) else 0f
        val displayRatio = if (isReverse) (1f - ratio).coerceIn(0f, 1f) else ratio.coerceIn(0f, 1f)
        
        val angle = 360 * displayRatio
        
        // Draw colored progress arc
        canvas.drawArc(rect, -90f, angle, false, progressPaint)

        // Draw center status text with auto-sizing
        if (centerText.isNotEmpty()) {
            val availableWidth = size - (padding * 2) - 30
            textPaint.textSize = 54f
            
            // Scale down text if it's too long
            var textWidth = textPaint.measureText(centerText)
            while (textWidth > availableWidth && textPaint.textSize > 12) {
                textPaint.textSize -= 2
                textWidth = textPaint.measureText(centerText)
            }
            
            val xPos = canvas.width / 2f
            val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(centerText, xPos, yPos, textPaint)
        }
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }
}