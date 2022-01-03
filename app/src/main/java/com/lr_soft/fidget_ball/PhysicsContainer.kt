package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.PointF

class PhysicsContainer(val width: Int, val height: Int) {
    val ball: Ball
    val obstacles: MutableList<Obstacle> = mutableListOf()

    init {
        ball = Ball(
            position = ConcurrentPointF(width / 2f, height / 2f),
            velocity = PointF(0f, 0f),
            radius = width / 100f
        )
    }


    fun draw(canvas: Canvas) {
        ball.draw(canvas)
    }
}