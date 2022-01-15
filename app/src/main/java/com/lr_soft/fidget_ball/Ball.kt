package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class Ball(
    val position: PointF,
    val radius: Float,
    color: Int = Color.BLUE
) {
    val velocity = PointF()

    var applyPhysics = false

    private val positionForDraw = ConcurrentPointF(position)

    private val paint = Paint().apply {
        this.color = color
        this.isAntiAlias = true
    }

    fun updatePositionForDraw() {
        positionForDraw.set(position)
    }

    fun draw(canvas: Canvas) {
        val position = positionForDraw.get()
        canvas.drawCircle(position.x, position.y, radius, paint)
    }
}

class ConcurrentPointF(pointF: PointF) {
    private var x: Float = pointF.x
    private var y: Float = pointF.y

    @Synchronized
    fun set(pointF: PointF) {
        x = pointF.x
        y = pointF.y
    }

    @Synchronized
    fun get(): PointF {
        return PointF(x, y)
    }
}