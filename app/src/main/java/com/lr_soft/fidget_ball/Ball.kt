package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import java.util.concurrent.atomic.AtomicLong

class Ball(
    val position: ConcurrentPointF,
    val velocity: PointF,
    val radius: Float,
    color: Int = Color.BLUE
) {
    private val paint = Paint().apply {
        this.color = color
        this.isAntiAlias = true
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(position.x, position.y, radius, paint)
    }
}

class ConcurrentPointF(x: Float, y: Float) {
    var x: Float = x
        @Synchronized get
        @Synchronized set

    var y: Float = y
        @Synchronized get
        @Synchronized set

    constructor() : this(0f, 0f)

    constructor(pointF: PointF) : this(pointF.x, pointF.y)

    @Synchronized
    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun set(pointF: PointF) {
        set(pointF.x, pointF.y)
    }

    fun toPointF(): PointF {
        return PointF(x, y)
    }
}