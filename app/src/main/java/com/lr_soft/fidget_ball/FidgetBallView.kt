package com.lr_soft.fidget_ball

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class FidgetBallView(context: Context): View(context) {
    private val paint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(300f, 300f, 100f, paint)
    }
}